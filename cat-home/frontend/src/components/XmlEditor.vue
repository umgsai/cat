<template>
  <div ref="editorHost" class="xml-editor"></div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { defaultKeymap, history, historyKeymap, indentWithTab } from '@codemirror/commands'
import { xml } from '@codemirror/lang-xml'
import { foldGutter, indentOnInput, syntaxHighlighting, HighlightStyle } from '@codemirror/language'
import { EditorView, keymap, lineNumbers } from '@codemirror/view'
import { tags } from '@lezer/highlight'
import { getEditorColors, onThemeChange } from '../theme'

const model = defineModel<string>({ default: '' })
const editorHost = ref<HTMLDivElement | null>(null)
let editorView: EditorView | null = null
let removeThemeListener: (() => void) | null = null

function buildHighlight() {
  const c = getEditorColors()
  return HighlightStyle.define([
    { tag: tags.tagName, color: c.tagName, fontWeight: '700' },
    { tag: tags.attributeName, color: c.attributeName },
    { tag: tags.attributeValue, color: c.attributeValue },
    { tag: tags.string, color: c.attributeValue },
    { tag: tags.angleBracket, color: '#667085' },
    { tag: tags.comment, color: '#667085', fontStyle: 'italic' },
    { tag: tags.meta, color: c.meta }
  ])
}

onMounted(() => {
  if (!editorHost.value) {
    return
  }

  editorView = new EditorView({
    doc: model.value,
    extensions: [
      lineNumbers(),
      foldGutter(),
      history(),
      indentOnInput(),
      xml(),
      syntaxHighlighting(buildHighlight()),
      keymap.of([indentWithTab, ...defaultKeymap, ...historyKeymap]),
      EditorView.lineWrapping,
      EditorView.updateListener.of((update) => {
        if (update.docChanged) {
          model.value = update.state.doc.toString()
        }
      })
    ],
    parent: editorHost.value
  })

  removeThemeListener = onThemeChange(() => {
    if (!editorView) {
      return
    }
    editorView.dispatch({
      effects: []
    })
  })
})

watch(model, (value) => {
  if (!editorView || value === editorView.state.doc.toString()) {
    return
  }
  editorView.dispatch({
    changes: {
      from: 0,
      to: editorView.state.doc.length,
      insert: value
    }
  })
})

onBeforeUnmount(() => {
  removeThemeListener?.()
  editorView?.destroy()
  editorView = null
})
</script>
