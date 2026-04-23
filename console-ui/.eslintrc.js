// ESlint 检查配置
module.exports = {
  root: true,
  parserOptions: {
    ecmaVersion: 2020,
    sourceType: 'module'
  },
  env: {
    browser: true,
    node: true,
    es6: true,
  },
  extends: ['plugin:vue/recommended', 'eslint:recommended'],

  // add your custom rules here
  //it is base on https://github.com/vuejs/eslint-config-vue
  rules: {
    // A 级：高风险/正确性规则（阻断）
    'constructor-super': 'error',
    'eqeqeq': ['error', 'always', { 'null': 'ignore' }],
    'no-class-assign': 'error',
    'no-cond-assign': 'error',
    'no-const-assign': 'error',
    'no-delete-var': 'error',
    'no-dupe-args': 'error',
    'no-dupe-class-members': 'error',
    'no-dupe-keys': 'error',
    'no-duplicate-case': 'error',
    'no-empty-character-class': 'error',
    'no-empty-pattern': 'error',
    'no-ex-assign': 'error',
    'no-func-assign': 'error',
    'no-invalid-regexp': 'error',
    'no-irregular-whitespace': 'error',
    'no-this-before-super': 'error',
    'no-undef': 'error',
    'no-unexpected-multiline': 'error',
    'no-unreachable': 'error',
    'no-unsafe-finally': 'error',
    'valid-typeof': 'error',
    'use-isnan': 'error',
    'no-debugger': process.env.NODE_ENV === 'production' ? 'error' : 'off',

    // B 级：质量提醒（不阻断）
    'no-unused-vars': ['warn', { 'vars': 'all', 'args': 'none' }],
    'prefer-const': 'warn',
    'no-console': 'warn',

    // C 级：风格类（阶段一先关闭，降低噪音）
    'vue/max-attributes-per-line': 'off',
    'vue/singleline-html-element-content-newline': 'off',
    'vue/multiline-html-element-content-newline': 'off',
    'vue/no-v-html': 'off',
    'array-bracket-spacing': 'off',
    'eol-last': 'off',
    'indent': 'off',
    'key-spacing': 'off',
    'no-multiple-empty-lines': 'off',
    'no-trailing-spaces': 'off',
    'object-curly-spacing': 'off',
    'padded-blocks': 'off',
    'quotes': 'off',
    'semi': 'off',
    'space-before-function-paren': 'off'
  }
}
