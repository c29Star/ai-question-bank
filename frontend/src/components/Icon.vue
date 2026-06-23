<template>
  <span v-html="svgContent" class="icon-svg"></span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 },
  color: { type: String, default: 'currentColor' },
})

// SVG 路径表（不带外层 <svg> 标签，由 buildSvg 包装）
const ICONS = {
  questions: `<path d="M4 4.5A2.5 2.5 0 0 1 6.5 2H20v18H6.5A2.5 2.5 0 0 0 4 22.5v-18z"/><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M9 7h7M9 11h5"/><circle cx="16" cy="14" r="3.5" fill="white"/><path d="M16 12.5v3l1.8 1"/>`,
  papers: `<rect x="5" y="3" width="14" height="18" rx="2"/><path d="M9 8h6M9 12h6M9 16h3"/><path d="M16.5 14.5l1.5 1.5 3-3"/>`,
  exams: `<circle cx="12" cy="13" r="7.5"/><path d="M12 9v4l2.5 1.5"/><path d="M9 3h6M12 3v2"/><path d="M5 6l-1 1M19 6l1 1"/>`,
  wrong: `<path d="M4 4h12a4 4 0 0 1 4 4v12H8a4 4 0 0 1-4-4V4z"/><path d="M4 4v16"/><path d="M8 10l2.5 2.5L15 8"/>`,
  stats: `<path d="M3 21h18"/><rect x="5" y="13" width="3" height="6" rx="0.5" fill="currentColor"/><rect x="10.5" y="9" width="3" height="10" rx="0.5" fill="currentColor"/><rect x="16" y="5" width="3" height="14" rx="0.5" fill="currentColor"/>`,
  dashboard: `<path d="M3 13a9 9 0 0 1 18 0"/><path d="M12 13l4-4"/><circle cx="12" cy="13" r="1.2" fill="currentColor"/><path d="M3 17h18"/>`,
  ai: `<path d="M12 3l1.8 4.6L18.5 9l-4.7 1.4L12 15l-1.8-4.6L5.5 9l4.7-1.4L12 3z" fill="currentColor" opacity="0.3"/><path d="M19 15l.7 1.8L21.5 17.5l-1.8.7L19 20l-.7-1.8L16.5 17.5l1.8-.7L19 15z" fill="currentColor" opacity="0.4"/><path d="M5 17l.5 1.3L6.8 18.8l-1.3.5L5 20.5l-.5-1.2L3.2 18.8l1.3-.5L5 17z" fill="currentColor" opacity="0.4"/>`,
  plus: `<circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/>`,
  search: `<circle cx="11" cy="11" r="7"/><path d="M16.5 16.5L21 21"/>`,
  refresh: `<path d="M3 12a9 9 0 0 1 15.5-6.3L21 8"/><path d="M21 3v5h-5"/><path d="M21 12a9 9 0 0 1-15.5 6.3L3 16"/><path d="M3 21v-5h5"/>`,
  upload: `<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M17 8l-5-5-5 5"/><path d="M12 3v12"/>`,
  download: `<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><path d="M7 10l5 5 5-5"/><path d="M12 15V3"/>`,
  list: `<path d="M8 6h13M8 12h13M8 18h13"/><circle cx="4" cy="6" r="1" fill="currentColor"/><circle cx="4" cy="12" r="1" fill="currentColor"/><circle cx="4" cy="18" r="1" fill="currentColor"/>`,
  grid: `<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>`,
  edit: `<path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/>`,
  delete: `<path d="M3 6h18"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6"/><path d="M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/><path d="M10 11v6M14 11v6"/>`,
  view: `<path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/>`,
  more: `<circle cx="5" cy="12" r="1.4" fill="currentColor"/><circle cx="12" cy="12" r="1.4" fill="currentColor"/><circle cx="19" cy="12" r="1.4" fill="currentColor"/>`,
  logout: `<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5"/><path d="M21 12H9"/>`,
  user: `<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/>`,
  clock: `<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>`,
  doc: `<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><path d="M8 13h8M8 17h5"/>`,
  send: `<path d="M22 2L11 13"/><path d="M22 2l-7 20-4-9-9-4 20-7z"/>`,
  monitor: `<path d="M3 12h4l3-9 4 18 3-9h4"/>`,
  check: `<path d="M5 12l5 5L20 7"/>`,
  close: `<path d="M6 6l12 12M18 6L6 18"/>`,
  warning: `<path d="M10.3 3.86l-7.4 12.85A2 2 0 0 0 4.62 19.5h14.76a2 2 0 0 0 1.72-2.79L13.7 3.86a2 2 0 0 0-3.4 0z"/><path d="M12 9v4M12 17h.01"/>`,
  loading: `<path d="M21 12a9 9 0 1 1-6.22-8.56"/>`,
  report: `<path d="M4 4h12a2 2 0 0 1 2 2v14H6a2 2 0 0 1-2-2V4z" fill="white"/><path d="M4 18a2 2 0 0 1 2-2h12"/><polyline points="7,14 10,11 12,13 16,8"/><circle cx="7" cy="14" r="1" fill="currentColor"/><circle cx="10" cy="11" r="1" fill="currentColor"/><circle cx="12" cy="13" r="1" fill="currentColor"/><circle cx="16" cy="8" r="1" fill="currentColor"/>`,
  subjects: `<path d="M3 21h18"/><rect x="4" y="13" width="3.5" height="6" rx="0.5" fill="currentColor" opacity="0.7"/><rect x="10" y="9" width="3.5" height="10" rx="0.5" fill="currentColor" opacity="0.85"/><rect x="16" y="5" width="3.5" height="14" rx="0.5" fill="currentColor"/>`,
  badge: `<circle cx="12" cy="10" r="6" fill="currentColor" fill-opacity="0.15"/><circle cx="12" cy="10" r="6"/><circle cx="12" cy="10" r="3"/><path d="M8 14.5L6 22l4-2 2 2 4-2-2-7.5" fill="currentColor" fill-opacity="0.2"/><path d="M16 14.5L18 22l-4-2-2 2-4-2 2-7.5"/><path d="M9 9l1.5 1.5L13 8"/>`,
  duration: `<circle cx="12" cy="13" r="8"/><path d="M12 8v5l3 2"/><path d="M9 3h6"/><path d="M12 3v2"/>`,
  flame: `<path d="M12 2c1 4 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z" fill="currentColor" fill-opacity="0.15"/><path d="M12 2c1 4 5 5 5 10a5 5 0 0 1-10 0c0-2 1-3 2-4 0 2 1 3 2 3 0-3-1-5 1-9z"/>`,
  target: `<circle cx="12" cy="12" r="9"/><circle cx="12" cy="12" r="5"/><circle cx="12" cy="12" r="1.5" fill="currentColor"/>`,
  rank: `<path d="M6 21V11h4v10"/><path d="M14 21V7h4v14"/><path d="M10 21V14h4v7"/><path d="M3 8l4-6 4 6"/><path d="M13 4l3-3 3 3"/>`,
  pdf: `<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><path d="M14 2v6h6"/><text x="7" y="17" fill="currentColor" font-size="5" font-weight="bold" font-family="Arial">PDF</text>`,
  fullscreen: `<path d="M3 9V3h6"/><path d="M21 9V3h-6"/><path d="M3 15v6h6"/><path d="M21 15v6h-6"/>`,
  review: `<path d="M3 12a9 9 0 1 0 3-6.7"/><path d="M3 4v5h5"/><path d="M12 8v4l3 2"/>`,
  correct: `<path d="M5 12l5 5L20 7"/>`,
  'wrong-x': `<path d="M6 6l12 12M18 6L6 18"/>`,
  tip: `<path d="M9 18h6"/><path d="M10 22h4"/><path d="M12 2a7 7 0 0 0-4 12.7c.7.6 1 1.4 1 2.3v1h6v-1c0-.9.3-1.7 1-2.3A7 7 0 0 0 12 2z" fill="currentColor" fill-opacity="0.15"/><path d="M12 2a7 7 0 0 0-4 12.7c.7.6 1 1.4 1 2.3v1h6v-1c0-.9.3-1.7 1-2.3A7 7 0 0 0 12 2z"/>`,
  radar: `<circle cx="12" cy="12" r="2" fill="currentColor"/><circle cx="12" cy="12" r="6"/><circle cx="12" cy="12" r="10"/><path d="M12 12L20 4"/>`,
  calendar: `<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M3 10h18"/><path d="M8 3v4M16 3v4"/>`,
  settings: `<circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h0a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h0a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v0a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"/>`,
}

const FALLBACK = `<circle cx="12" cy="12" r="4" fill="currentColor"/>`

const svgContent = computed(() => {
  const path = ICONS[props.name] || FALLBACK
  return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="${props.size}" height="${props.size}" fill="none" stroke="${props.color}" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" style="display:inline-block;vertical-align:-3px">${path}</svg>`
})
</script>

<style scoped>
.icon-svg { display: inline-flex; line-height: 0; }
.icon-svg :deep(svg) { display: block; }
</style>
