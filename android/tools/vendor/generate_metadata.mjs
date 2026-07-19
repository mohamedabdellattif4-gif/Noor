import { getAyahMeta, getSurahMeta, meta } from '/mnt/data/pypi/quran-meta/package/dist/hafs.js';
const out = { meta, ayahs: [], surahs: [] };
for (let id = 1; id <= meta.numAyahs; id++) out.ayahs.push({ id, ...getAyahMeta(id) });
for (let number = 1; number <= meta.numSurahs; number++) out.surahs.push(getSurahMeta(number));
process.stdout.write(JSON.stringify(out));
