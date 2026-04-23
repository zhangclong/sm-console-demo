import hasRole from './hasRole'
import hasPermi from './hasPermi'

export default {
  install(app) {
    app.directive('hasRole', hasRole)
    app.directive('hasPermi', hasPermi)
  },
}
