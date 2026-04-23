import { defineStore } from 'pinia'

export const useDictStore = defineStore('dict', {
  state: () => ({
    dict: [],
  }),
  actions: {
    setDict(key, value) {
      if (key !== null && key !== '') {
        this.dict.push({ key, value })
      }
    },
    removeDict(key) {
      const i = this.dict.findIndex((d) => d.key === key)
      if (i > -1) this.dict.splice(i, 1)
    },
    cleanDict() {
      this.dict = []
    },
  },
})
