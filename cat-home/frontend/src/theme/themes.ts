export interface ThemeColors {
  primary: string
  primaryHover: string
  primaryBgActive: string
  primaryBgHover: string
  primaryBgSubtle: string
  primaryBorder: string
  primaryAccent: string
  primaryTextDark: string
  primaryTextDarker: string
  primaryFocusRing: string
  loginGradientStart: string
  loginGradientEnd: string
  loginEyebrow: string
  loginActiveLine: string
  loginSelection: string
  chartPalette: string[]
  chartAxisColor: string
  chartSplitColor: string
  chartTextColor: string
  chartTextColorDark: string
  editorTagName: string
  editorAttributeName: string
  editorAttributeValue: string
  editorMeta: string
}

export interface ThemeDefinition {
  id: string
  label: string
  swatch: string
  colors: ThemeColors
}

export const themes: Record<string, ThemeDefinition> = {
  teal: {
    id: 'teal',
    label: 'Teal',
    swatch: '#0f766e',
    colors: {
      primary: '#0f766e',
      primaryHover: '#115e59',
      primaryBgActive: '#e5f5f2',
      primaryBgHover: '#eef5f4',
      primaryBgSubtle: '#eef7f6',
      primaryBorder: '#9ccfc7',
      primaryAccent: '#ccebe5',
      primaryTextDark: '#0b6f67',
      primaryTextDarker: '#0b5f59',
      primaryFocusRing: 'rgba(15, 118, 110, 0.12)',
      loginGradientStart: 'rgba(8, 76, 97, 0.94)',
      loginGradientEnd: 'rgba(39, 92, 104, 0.86)',
      loginEyebrow: '#a7f3d0',
      loginActiveLine: '#eef7f6',
      loginSelection: '#ccebe5',
      chartPalette: ['#7cb5ec', '#434348', '#90ed7d', '#f7a35c', '#8085e9', '#f15c80'],
      chartAxisColor: '#ccd6e0',
      chartSplitColor: '#d7d7d7',
      chartTextColor: '#666',
      chartTextColorDark: '#444',
      editorTagName: '#0f766e',
      editorAttributeName: '#7c3aed',
      editorAttributeValue: '#b45309',
      editorMeta: '#0369a1'
    }
  },
  blue: {
    id: 'blue',
    label: 'Blue',
    swatch: '#1d4ed8',
    colors: {
      primary: '#1d4ed8',
      primaryHover: '#1e40af',
      primaryBgActive: '#eff6ff',
      primaryBgHover: '#eff6ff',
      primaryBgSubtle: '#f0f5ff',
      primaryBorder: '#93c5fd',
      primaryAccent: '#dbeafe',
      primaryTextDark: '#1e40af',
      primaryTextDarker: '#1e3a8a',
      primaryFocusRing: 'rgba(37, 99, 235, 0.12)',
      loginGradientStart: 'rgba(30, 64, 175, 0.94)',
      loginGradientEnd: 'rgba(37, 99, 235, 0.86)',
      loginEyebrow: '#bfdbfe',
      loginActiveLine: '#eff6ff',
      loginSelection: '#dbeafe',
      chartPalette: ['#3b82f6', '#434348', '#90ed7d', '#f7a35c', '#8085e9', '#f15c80'],
      chartAxisColor: '#ccd6e0',
      chartSplitColor: '#d7d7d7',
      chartTextColor: '#666',
      chartTextColorDark: '#444',
      editorTagName: '#1d4ed8',
      editorAttributeName: '#7c3aed',
      editorAttributeValue: '#b45309',
      editorMeta: '#0369a1'
    }
  },
  purple: {
    id: 'purple',
    label: 'Purple',
    swatch: '#7c3aed',
    colors: {
      primary: '#7c3aed',
      primaryHover: '#6d28d9',
      primaryBgActive: '#f5f3ff',
      primaryBgHover: '#f5f3ff',
      primaryBgSubtle: '#faf5ff',
      primaryBorder: '#c4b5fd',
      primaryAccent: '#ede9fe',
      primaryTextDark: '#6d28d9',
      primaryTextDarker: '#5b21b6',
      primaryFocusRing: 'rgba(124, 58, 237, 0.12)',
      loginGradientStart: 'rgba(76, 29, 149, 0.94)',
      loginGradientEnd: 'rgba(107, 33, 168, 0.86)',
      loginEyebrow: '#ddd6fe',
      loginActiveLine: '#faf5ff',
      loginSelection: '#ede9fe',
      chartPalette: ['#8b5cf6', '#434348', '#90ed7d', '#f7a35c', '#8085e9', '#f15c80'],
      chartAxisColor: '#ccd6e0',
      chartSplitColor: '#d7d7d7',
      chartTextColor: '#666',
      chartTextColorDark: '#444',
      editorTagName: '#7c3aed',
      editorAttributeName: '#7c3aed',
      editorAttributeValue: '#b45309',
      editorMeta: '#0369a1'
    }
  }
}

export const DEFAULT_THEME = 'teal'

export const themeList = Object.values(themes)
