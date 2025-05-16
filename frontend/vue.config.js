'use strict'
const path = require('path')
const webpack = require('webpack')
const CompressionPlugin = require('compression-webpack-plugin')

function resolve(dir) {
  return path.join(__dirname, dir);
}

// const publicPath = process.env.VUE_APP_HISTORY === 'y' ? process.env.VUE_APP_BASE + '/' : ''

const name = process.env.VUE_APP_TITLE || 'SiWU-IoT-VIEWS'
const port = process.env.port || process.env.npm_config_port || 80

module.exports = {
  publicPath: process.env.NODE_ENV === "production" ? "/" : "/",
  outputDir: 'dist',
  assetsDir: 'static',
  lintOnSave: process.env.NODE_ENV === 'development',
  productionSourceMap: false,
  transpileDependencies: ['quill', '@antv/*'],
  devServer: {
    host: "0.0.0.0",
    port: port,
    open: true,
    proxy: {
      [process.env.VUE_APP_BASE_API]: {
        target: `http://localhost:9712/`,
        changeOrigin: true,
        pathRewrite: {
          ["^" + process.env.VUE_APP_BASE_API]: "",
        },
      },
    },
    disableHostCheck: true,
  },
  css: {
    loaderOptions: {
      sass: {
        sassOptions: { outputStyle: "expanded" },
      },
    },
  },
  configureWebpack: {
    name: name,  
    resolve: {
      extensions: ['.js', '.vue', '.json', '.mjs'], // 支持 .mjs 文件
      alias: {
        '@': resolve('src'),
        vue$: 'vue/dist/vue.common',
        'data-room-ui': resolve('packages'),
        '@gcpaas/data-room-ui': resolve('packages/index.js')
      }
    },
    // externals: {
    //   'three': 'THREE',
    //   'GLTFLoader': 'GLTFLoader',
    //   'OrbitControls': 'OrbitControls',
    //   'RGBELoader': 'RGBELoader',
    //   'Sky': 'Sky'
    // },
    module: {
      rules: [
        {
          test: /\.m?js$/,
          exclude: /(node_modules|bower_components)/,
          use: {
            loader: 'babel-loader',
            options: {
              presets: ['@babel/preset-env']
            }
          }
        },
        {
          test: /\.mjs$/,
          type: "javascript/auto", // 确保 Webpack 正确解析 .mjs 文件
          use: {
            loader: 'babel-loader',
            options: {
              presets: ['@babel/preset-env']
            }
          }
        }
      ]
    },
    plugins: [
      new CompressionPlugin({
        cache: false,
        test: /\.(js|css|html|jpe?g|png|gif|svg)?$/i,
        filename: '[path][base].gz[query]',
        algorithm: 'gzip',
        minRatio: 0.8,
        deleteOriginalAssets: false
      }),
      new webpack.ProvidePlugin({
        jQuery: 'jquery',
        $: 'jquery',
        'windows.jQuery': 'jquery'
      })
    ]
  },
  chainWebpack(config) {
    config.plugins.delete('preload')
    config.plugins.delete('prefetch')

    config.module
      .rule('svg')
      .exclude.add(resolve('src/assets/icons'))
      .add(resolve('packages/assets/images/dataSourceIcon/svg'))
      .add(resolve('packages/assets/images/pageIcon/svg'))
      .add(resolve('packages/assets/images/bigScreenIcon/svg'))
      .add(resolve('packages/Svgs/svg'))
      .add(resolve('packages/assets/images/alignIcon/svg'))
      .end()

    config.module
      .rule("icons")
      .test(/\.svg$/)
      .include.add(resolve("src/assets/icons"))
      .add(resolve("packages/assets/images/dataSourceIcon/svg"))
      .add(resolve("packages/assets/images/pageIcon/svg"))
      .add(resolve("packages/assets/images/bigScreenIcon/svg"))
      .add(resolve("packages/Svgs/svg"))
      .add(resolve("packages/assets/images/alignIcon/svg"))
      .end()
      .use("svg-sprite-loader")
      .loader("svg-sprite-loader")
      .options({
        symbolId: "icon-[name]",
      })
      .end();

    config.when(process.env.NODE_ENV !== "development", (config) => {
      config
        .plugin('ScriptExtHtmlWebpackPlugin')
        .after('html')
        .use('script-ext-html-webpack-plugin', [{
          inline: /runtime\..*\.js$/
        }])
        .end()

      config.optimization.splitChunks({
        chunks: "all",
        cacheGroups: {
          libs: {
            name: "chunk-libs",
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial'
          },
          elementUI: {
            name: 'chunk-elementUI',
            test: /[\\/]node_modules[\\/]_?element-ui(.*)/,
            priority: 20
          },
          commons: {
            name: 'chunk-commons',
            test: resolve('src/components'),
            minChunks: 3,
            priority: 5,
            reuseExistingChunk: true
          }
        }
      })
      config.optimization.runtimeChunk('single')
    })
  }
}
