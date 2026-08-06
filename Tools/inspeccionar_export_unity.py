#!/usr/bin/env python3
"""
Inspecciona un export de Unity y dice QUÉ ESCENAS trae de verdad.

    python3 Tools/inspeccionar_export_unity.py unityLibrary/src/main/assets/bin/Data/data.unity3d

Sirve para validar un export **antes** de gastar los ~25 minutos de compilación
de IL2CPP. Si las escenas que lista no son las 12 esperadas, el export está mal
y hay que devolverlo.

⚠️ POR QUÉ EXISTE ESTE SCRIPT
`strings data.unity3d | grep level` MIENTE. El listado de nodos del bundle va
comprimido con LZ4, y nombres consecutivos como `level1`…`level6` se codifican
como referencias hacia atrás a `level0`: al pasarles `strings` solo aparece
`level0` y parece que el export trae UNA escena cuando trae siete. Lo mismo
pasa con los nombres de escena del BuildSettings. Hay que descomprimir de
verdad, que es lo que hace esto (sin dependencias externas).

Formato UnityFS, por si hay que tocarlo:
  header: "UnityFS" | version(u32 BE) | unityVersion | unityRevision |
          size(i64) | compressedBlocksInfoSize(u32) |
          uncompressedBlocksInfoSize(u32) | flags(u32)
  flags: 0x3F = tipo de compresión (0 none, 1 LZMA, 2 LZ4, 3 LZ4HC)
         0x80 = blocksInfo al final del archivo
         0x200 = los datos arrancan alineados a 16 bytes  <-- fácil de pasar por alto
"""
import re
import struct
import sys


def lz4_decompress(src: bytes, uncompressed_size: int) -> bytes:
    """LZ4 block format, a mano para no depender del paquete `lz4`."""
    dst = bytearray()
    i, n = 0, len(src)
    while i < n:
        token = src[i]; i += 1
        lit_len = token >> 4
        if lit_len == 15:
            while True:
                b = src[i]; i += 1
                lit_len += b
                if b != 255:
                    break
        dst += src[i:i + lit_len]
        i += lit_len
        if i >= n:
            break
        offset = src[i] | (src[i + 1] << 8); i += 2
        match_len = token & 0x0F
        if match_len == 15:
            while True:
                b = src[i]; i += 1
                match_len += b
                if b != 255:
                    break
        match_len += 4
        start = len(dst) - offset
        for k in range(match_len):
            dst.append(dst[start + k])
    return bytes(dst[:uncompressed_size])


def _cstr(buf: bytes, pos: int):
    end = buf.index(b"\0", pos)
    return buf[pos:end].decode("utf-8", "replace"), end + 1


def inspeccionar(path: str) -> int:
    data = open(path, "rb").read()

    pos = 0
    sig, pos = _cstr(data, pos)
    if sig != "UnityFS":
        print(f"  ✗ No parece un bundle UnityFS (firma: {sig!r})")
        return 1
    version = struct.unpack_from(">I", data, pos)[0]; pos += 4
    _, pos = _cstr(data, pos)                      # unityVersion ("5.x.x")
    revision, pos = _cstr(data, pos)               # p.ej. "2021.3.4f1"
    pos += 8                                       # size
    comp_bi = struct.unpack_from(">I", data, pos)[0]; pos += 4
    uncomp_bi = struct.unpack_from(">I", data, pos)[0]; pos += 4
    flags = struct.unpack_from(">I", data, pos)[0]; pos += 4

    print(f"  Unity {revision} · bundle v{version}")

    if version >= 7:
        pos = (pos + 15) & ~15

    al_final = bool(flags & 0x80)
    bloque_bi = data[-comp_bi:] if al_final else data[pos:pos + comp_bi]
    if not al_final:
        pos += comp_bi
    bi = lz4_decompress(bloque_bi, uncomp_bi) if (flags & 0x3F) in (2, 3) else bloque_bi
    if flags & 0x200:
        pos = (pos + 15) & ~15

    p = 16                                          # hash
    nblocks = struct.unpack_from(">i", bi, p)[0]; p += 4
    bloques = []
    for _ in range(nblocks):
        us, cs, bf = struct.unpack_from(">IIH", bi, p); p += 10
        bloques.append((us, cs, bf))
    nnodes = struct.unpack_from(">i", bi, p)[0]; p += 4
    nodos = []
    for _ in range(nnodes):
        off, sz, _nf = struct.unpack_from(">qqI", bi, p); p += 20
        nombre, p = _cstr(bi, p)
        nodos.append((nombre, off, sz))

    escenas_bin = [n for n, _, _ in nodos if re.fullmatch(r"level\d+", n)]
    print(f"  Escenas empaquetadas: {len(escenas_bin)}  ({', '.join(escenas_bin) or 'ninguna'})")

    # Los NOMBRES de las escenas viven en el BuildSettings, dentro de
    # globalgamemanagers. Hay que descomprimir bloques hasta alcanzarlo.
    objetivo = next((n for n in nodos if n[0] == "globalgamemanagers"), None)
    if objetivo is None:
        print("  ✗ No hay globalgamemanagers; no se pueden leer los nombres.")
        return 1

    necesario = objetivo[1] + objetivo[2]
    out = bytearray()
    cur = pos
    for us, cs, bf in bloques:
        chunk = data[cur:cur + cs]; cur += cs
        out += chunk if (bf & 0x3F) == 0 else lz4_decompress(chunk, us)
        if len(out) >= necesario:
            break

    ggm = bytes(out[objetivo[1]:objetivo[1] + objetivo[2]])
    rutas = re.findall(rb"Assets/[^\x00]{0,120}?\.unity", ggm)

    print(f"  Escenas en Build Settings ({len(rutas)}), en orden:")
    for i, r in enumerate(rutas):
        nombre = r.decode().rsplit("/", 1)[-1][:-len(".unity")]
        marca = "   ← índice 0, es la que arranca" if i == 0 else ""
        print(f"    {i:2d}  {nombre}{marca}")

    nombres = {r.decode().rsplit('/', 1)[-1][:-len('.unity')] for r in rutas}
    esperadas = {
        "AuraNeutral", "AuraRoja", "AuraNaranja", "AuraVerde", "AuraAzul",
        "AuraVioleta", "AuraRosa",
        "FiltroMariposas", "FiltroLuciernagas", "FiltroLluvia", "FiltroFuego",
        "FiltroDragonBall",
    }
    faltan = esperadas - nombres
    sobran = nombres - esperadas
    print()
    if faltan:
        print(f"  ✗ FALTAN {len(faltan)}: {', '.join(sorted(faltan))}")
    if sobran:
        print(f"  ⚠ No esperadas: {', '.join(sorted(sobran))}")
    if not faltan and rutas and rutas[0].decode().endswith("AuraNeutral.unity"):
        print("  ✓ Las 12 escenas están y AuraNeutral es la de arranque.")
    elif not faltan:
        print("  ⚠ Están las 12, pero la escena de arranque NO es AuraNeutral.")
    return 0


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    for arg in sys.argv[1:]:
        print(f"########## {arg} ##########")
        inspeccionar(arg)
        print()
