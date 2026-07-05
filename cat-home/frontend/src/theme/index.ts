import { DEFAULT_THEME, themes, type ThemeColors } from './themes'

const STORAGE_KEY = 'cat-theme'
const THEME_EVENT = 'themeChanged'

let currentThemeId: string = DEFAULT_THEME

export function initTheme(): void {
  const stored = readStoredTheme()
  const themeId = stored || DEFAULT_THEME
  applyTheme(themeId)
}

export function getCurrentThemeId(): string {
  return currentThemeId
}

export function getCurrentColors(): ThemeColors {
  return themes[currentThemeId]?.colors || themes[DEFAULT_THEME].colors
}

export function setTheme(themeId: string): void {
  const theme = themes[themeId]
  if (!theme) {
    return
  }
  applyTheme(themeId)
  try {
    window.localStorage.setItem(STORAGE_KEY, themeId)
  } catch {
    // localStorage may be unavailable (private mode, etc.)
  }
  window.dispatchEvent(new CustomEvent(THEME_EVENT, { detail: { themeId } }))
}

export function onThemeChange(callback: (themeId: string) => void): () => void {
  const handler = (event: Event) => {
    const customEvent = event as CustomEvent
    callback(customEvent.detail?.themeId || currentThemeId)
  }
  window.addEventListener(THEME_EVENT, handler)
  return () => window.removeEventListener(THEME_EVENT, handler)
}

export function getChartColors(): { palette: string[]; axisColor: string; splitColor: string; textColor: string; textColorDark: string } {
  const c = getCurrentColors()
  return {
    palette: c.chartPalette,
    axisColor: c.chartAxisColor,
    splitColor: c.chartSplitColor,
    textColor: c.chartTextColor,
    textColorDark: c.chartTextColorDark
  }
}

export function getEditorColors(): { tagName: string; attributeName: string; attributeValue: string; meta: string } {
  const c = getCurrentColors()
  return {
    tagName: c.editorTagName,
    attributeName: c.editorAttributeName,
    attributeValue: c.editorAttributeValue,
    meta: c.editorMeta
  }
}

function applyTheme(themeId: string): void {
  const theme = themes[themeId]
  if (!theme) {
    return
  }
  currentThemeId = themeId
  const root = document.documentElement
  root.dataset.theme = themeId
  const c = theme.colors
  const cssVars: Record<string, string> = {
    '--color-primary': c.primary,
    '--color-primary-hover': c.primaryHover,
    '--color-primary-bg-active': c.primaryBgActive,
    '--color-primary-bg-hover': c.primaryBgHover,
    '--color-primary-bg-subtle': c.primaryBgSubtle,
    '--color-primary-border': c.primaryBorder,
    '--color-primary-accent': c.primaryAccent,
    '--color-primary-text-dark': c.primaryTextDark,
    '--color-primary-text-darker': c.primaryTextDarker,
    '--color-primary-focus-ring': c.primaryFocusRing,
    '--login-gradient-start': c.loginGradientStart,
    '--login-gradient-end': c.loginGradientEnd,
    '--login-eyebrow': c.loginEyebrow,
    '--login-active-line': c.loginActiveLine,
    '--login-selection': c.loginSelection,
    '--el-color-primary': c.primary,
    '--el-color-primary-light-3': lighten(c.primary, 0.3),
    '--el-color-primary-light-5': lighten(c.primary, 0.5),
    '--el-color-primary-light-7': lighten(c.primary, 0.7),
    '--el-color-primary-light-8': lighten(c.primary, 0.8),
    '--el-color-primary-light-9': lighten(c.primary, 0.9),
    '--el-color-primary-dark-2': c.primaryHover
  }
  for (const [key, value] of Object.entries(cssVars)) {
    root.style.setProperty(key, value)
  }
}

function readStoredTheme(): string | null {
  try {
    return window.localStorage.getItem(STORAGE_KEY)
  } catch {
    return null
  }
}

function lighten(hex: string, amount: number): string {
  const rgb = hexToRgb(hex)
  if (!rgb) {
    return hex
  }
  const r = Math.round(rgb.r + (255 - rgb.r) * amount)
  const g = Math.round(rgb.g + (255 - rgb.g) * amount)
  const b = Math.round(rgb.b + (255 - rgb.b) * amount)
  return `#${r.toString(16).padStart(2, '0')}${g.toString(16).padStart(2, '0')}${b.toString(16).padStart(2, '0')}`
}

function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
  const match = hex.match(/^#([0-9a-fA-F]{6})$/)
  if (!match) {
    return null
  }
  const value = match[1]
  return {
    r: parseInt(value.substring(0, 2), 16),
    g: parseInt(value.substring(2, 4), 16),
    b: parseInt(value.substring(4, 6), 16)
  }
}
