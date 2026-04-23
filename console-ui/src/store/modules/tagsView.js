import { defineStore } from 'pinia'

export const useTagsViewStore = defineStore('tagsView', {
  state: () => ({
    visitedViews: [],
    cachedViews: [],
    iframeViews: [],
  }),
  actions: {
    addView(view) {
      this.addVisitedView(view)
      this.addCachedView(view)
    },
    addIframeView(view) {
      if (!this.iframeViews.some((v) => v.path === view.path)) {
        this.iframeViews.push(Object.assign({}, view, { title: view.meta?.title || 'no-name' }))
      }
    },
    addVisitedView(view) {
      if (!this.visitedViews.some((v) => v.path === view.path)) {
        this.visitedViews.push(Object.assign({}, view, { title: view.meta?.title || 'no-name' }))
      }
    },
    addCachedView(view) {
      if (this.cachedViews.includes(view.name)) return
      if (view.meta && !view.meta.noCache) {
        this.cachedViews.push(view.name)
      }
    },
    delView(view) {
      this.delVisitedView(view)
      this.delCachedView(view)
      return { visitedViews: [...this.visitedViews], cachedViews: [...this.cachedViews] }
    },
    delVisitedView(view) {
      const i = this.visitedViews.findIndex((v) => v.path === view.path)
      if (i > -1) this.visitedViews.splice(i, 1)
      this.iframeViews = this.iframeViews.filter((item) => item.path !== view.path)
      return [...this.visitedViews]
    },
    delIframeView(view) {
      this.iframeViews = this.iframeViews.filter((item) => item.path !== view.path)
      return [...this.iframeViews]
    },
    delCachedView(view) {
      const i = this.cachedViews.indexOf(view.name)
      if (i > -1) this.cachedViews.splice(i, 1)
      return [...this.cachedViews]
    },
    delOthersViews(view) {
      this.delOthersVisitedViews(view)
      this.delOthersCachedViews(view)
      return { visitedViews: [...this.visitedViews], cachedViews: [...this.cachedViews] }
    },
    delOthersVisitedViews(view) {
      this.visitedViews = this.visitedViews.filter((v) => v.meta?.affix || v.path === view.path)
      this.iframeViews = this.iframeViews.filter((item) => item.path === view.path)
      return [...this.visitedViews]
    },
    delOthersCachedViews(view) {
      const i = this.cachedViews.indexOf(view.name)
      this.cachedViews = i > -1 ? this.cachedViews.slice(i, i + 1) : []
      return [...this.cachedViews]
    },
    delAllViews() {
      this.delAllVisitedViews()
      this.delAllCachedViews()
      return { visitedViews: [...this.visitedViews], cachedViews: [...this.cachedViews] }
    },
    delAllVisitedViews() {
      this.visitedViews = this.visitedViews.filter((tag) => tag.meta?.affix)
      this.iframeViews = []
      return [...this.visitedViews]
    },
    delAllCachedViews() {
      this.cachedViews = []
      return [...this.cachedViews]
    },
    updateVisitedView(view) {
      for (let v of this.visitedViews) {
        if (v.path === view.path) {
          Object.assign(v, view)
          break
        }
      }
    },
    delRightTags(view) {
      const index = this.visitedViews.findIndex((v) => v.path === view.path)
      if (index === -1) return [...this.visitedViews]
      this.visitedViews = this.visitedViews.filter((item, idx) => {
        if (idx <= index || item.meta?.affix) return true
        const i = this.cachedViews.indexOf(item.name)
        if (i > -1) this.cachedViews.splice(i, 1)
        if (item.meta?.link) {
          const fi = this.iframeViews.findIndex((v) => v.path === item.path)
          if (fi > -1) this.iframeViews.splice(fi, 1)
        }
        return false
      })
      return [...this.visitedViews]
    },
    delLeftTags(view) {
      const index = this.visitedViews.findIndex((v) => v.path === view.path)
      if (index === -1) return [...this.visitedViews]
      this.visitedViews = this.visitedViews.filter((item, idx) => {
        if (idx >= index || item.meta?.affix) return true
        const i = this.cachedViews.indexOf(item.name)
        if (i > -1) this.cachedViews.splice(i, 1)
        if (item.meta?.link) {
          const fi = this.iframeViews.findIndex((v) => v.path === item.path)
          if (fi > -1) this.iframeViews.splice(fi, 1)
        }
        return false
      })
      return [...this.visitedViews]
    },
  },
})
