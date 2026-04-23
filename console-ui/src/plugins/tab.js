import router from '@/router'
import { useTagsViewStore } from '@/store/modules/tagsView'

export default {
  refreshPage(obj) {
    const tagsViewStore = useTagsViewStore()
    const { path, query, matched } = router.currentRoute.value
    if (obj === undefined) {
      matched.forEach((m) => {
        if (m.components && m.components.default && m.components.default.name) {
          if (!['Layout', 'ParentView'].includes(m.components.default.name)) {
            obj = { name: m.components.default.name, path: path, query: query }
          }
        }
      })
    }
    return tagsViewStore.delCachedView(obj).then(() => {
      const { path, query } = obj
      router.replace({ path: '/redirect' + path, query: query })
    })
  },
  closeOpenPage(obj) {
    const tagsViewStore = useTagsViewStore()
    tagsViewStore.delView(router.currentRoute.value)
    if (obj !== undefined) {
      return router.push(obj)
    }
    return Promise.resolve()
  },
  closePage(obj) {
    const tagsViewStore = useTagsViewStore()
    if (obj === undefined) {
      return tagsViewStore.delView(router.currentRoute.value).then(({ visitedViews }) => {
        const lastView = visitedViews.slice(-1)[0]
        if (lastView) {
          return router.push(lastView.fullPath)
        }
        return router.push('/')
      })
    }
    return Promise.resolve(tagsViewStore.delView(obj))
  },
  closeAllPage() {
    const tagsViewStore = useTagsViewStore()
    return Promise.resolve(tagsViewStore.delAllViews())
  },
  closeLeftPage(obj) {
    const tagsViewStore = useTagsViewStore()
    return Promise.resolve(tagsViewStore.delLeftTags(obj || router.currentRoute.value))
  },
  closeRightPage(obj) {
    const tagsViewStore = useTagsViewStore()
    return Promise.resolve(tagsViewStore.delRightTags(obj || router.currentRoute.value))
  },
  closeOtherPage(obj) {
    const tagsViewStore = useTagsViewStore()
    return Promise.resolve(tagsViewStore.delOthersViews(obj || router.currentRoute.value))
  },
  openPage(title, url, params) {
    const tagsViewStore = useTagsViewStore()
    var obj = { path: url, meta: { title: title } }
    tagsViewStore.addView(obj)
    return router.push({ path: url, query: params })
  },
  updatePage(obj) {
    const tagsViewStore = useTagsViewStore()
    return tagsViewStore.updateVisitedView(obj)
  },
}
