/* eslint consistent-return:0 */

const express = require('express');
const logger = require('./logger');

const argv = require('minimist')(process.argv.slice(2));
const setup = require('./middlewares/frontendMiddleware');
const isDev = process.env.NODE_ENV !== 'production';
const ngrok = (isDev && process.env.ENABLE_TUNNEL) || argv.tunnel ? require('ngrok') : false;
const resolve = require('path').resolve;
const proxy = require('http-proxy-middleware');
const app = express();

let controllerBackend = '192.168.99.100:31313';
if (process.env.FISSION_CONTROLLER !== undefined) {
  controllerBackend = process.env.FISSION_CONTROLLER;
}

let routerBackend = '192.168.99.100:31314';
if (process.env.FISSION_ROUTER !== undefined) {
  routerBackend = process.env.FISSION_ROUTER;
}

// Setup proxy for fission APIs
app.use('/proxy/controller', proxy({ target: `http://${controllerBackend}`, pathRewrite: { '^/proxy/controller': '' } }));
app.use('/proxy/router', proxy({ target: `http://${routerBackend}`, pathRewrite: { '^/proxy/router': '' } }));

// In production we need to pass these values in instead of relying on webpack
setup(app, {
  outputPath: resolve(process.cwd(), 'build'),
  publicPath: '/',
});


// get the intended host and port number, use localhost and port 3000 if not provided
const customHost = argv.host || process.env.HOST;
const host = customHost || null; // Let http.Server use its default IPv6/4 host
const prettyHost = customHost || 'localhost';

const port = argv.port || process.env.PORT || 3000;

// Start your app.
app.listen(port, host, (err) => {
  if (err) {
    return logger.error(err.message);
  }

  // Connect to ngrok in dev mode
  if (ngrok) {
    ngrok.connect(port, (innerErr, url) => {
      if (innerErr) {
        return logger.error(innerErr);
      }

      logger.appStarted(port, prettyHost, url);
    });
  } else {
    logger.appStarted(port, prettyHost);
  }
});
