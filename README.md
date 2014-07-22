# lein-asset-minifier

Lein-asset-minifier is a [Leiningen](https://github.com/technomancy/leiningen) plugin for CSS/JavaScript asset minification.

## Usage

To use `lein-asset-minifier`, add it as a plugin to your `project.clj` file:

[![Clojars Project](http://clojars.org/lein-asset-minifier/latest-version.svg)](http://clojars.org/lein-asset-minifier)

Then add a new `:minify-assets` key to your `project.clj` file that contains a map of configuration options.
At minimum there must be an `:assets` key that specifies the assets to minify.

The assets are specified using a map where the key is a string that is the name of the minified file and the
value points to the assets to minify. The assets can be either a filename, a directory, or a vector containing
a mix of files and directories.

```clojure
:minify-assets
{:assets
  {"resources/public/js/site.min.css" "dev/resources/css"
   "resources/public/js/vendor.min.css" "dev/resources/vendor"
   "resources/public/js/site.min.js" "dev/resources/js"
   "resources/public/js/vendor.min.js" ["dev/resources/vendor1"
                                        "dev/resources/vendor2"
                                        "dev/resources/some-script.js"]}}
```

The minifier also takes optional minification hints:

```clojure
:minify-assets
{:assets
  {"site.min.css" "dev/resources/css"}
 :options {:linebreak 80
           :optimization :advanced
           :externs ["jquery.min.js"]}}
```

* `:linebreak` - specifies optional linebreak for CSS resources
* `:optimizations` - specifies the level of JavaScript optimizations, valid values are `:simple`, `:whitespace` or `:advanced`, defaults to `:simple`.
* `:externs` - can be used to specify the externs file to be used with the advanced optimisations to prevent munging of external functions.

The plugin can be now be invoked by running:


```
lein minify-assets
```

## License

Copyright © 2014 Yogthos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
