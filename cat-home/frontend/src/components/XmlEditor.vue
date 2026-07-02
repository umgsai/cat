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

const model = defineModel<string>({ default: '' })
const editorHost = ref<HTMLDivElement | null>(null)
let editorView: EditorView | null = null

const xmlHighlight = HighlightStyle.define([
  { tag: tags.tagName, color: '#0f766e', fontWeight: '700' },
  { tag: tags.attributeName, color: '#7c3aed' },
  { tag: tags.attributeValue, color: '#b45309' },
  { tag: tags.string, color: '#b45309' },
  { tag: tags.angleBracket, color: '#667085' },
  { tag: tags.comment, color: '#667085', fontStyle: 'italic' },
  { tag: tags.meta, color: '#0369a1' }
])

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
      syntaxHighlighting(xmlHighlight),
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
  editorView?.destroy()
  editorView = null
})
</script>
