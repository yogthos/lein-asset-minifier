# lein-asset-minifier

Lein-asset-minifier is a [Leiningen](https://github.com/technomancy/leiningen) plugin for CSS/JavaScript asset minification. The plugin uses the [asset-minifier](https://github.com/yogthos/asset-minifier) to do minifcation.

## Usage

To use `lein-asset-minifier`, add it as a plugin to your `project.clj` file:

[![Clojars Project](http://clojars.org/lein-asset-minifier/latest-version.svg)](https://clojars.org/lein-asset-minifier)

Then add a new `:minify-assets` key to your `project.clj` file that contains a map of configuration options.
At minimum there must be an `:assets` key that specifies the assets to minify.

The assets are specified using a map where the key is a string that is the name of the minified file and the
value points to the assets to minify. The assets can be either a filename, a directory, or a vector containing
a mix of files and directories.

```clojure
:minify-assets
{:assets
  {"resources/public/css/site.min.css" "dev/resources/css"
   "resources/public/css/vendor.min.css" "dev/resources/vendor"
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
* `:optimizations` - specifies the level of JavaScript optimizations, valid values are `:none`, `:simple`, `:whitespace` or `:advanced`, defaults to `:simple`
* `:externs` - can be used to specify the externs file to be used with the advanced optimisations to prevent munging of external functions

The plugin can be now be invoked by running:

```
lein minify-assets

```

It's also possible to specify different profiles for asset minification as follows:

```clojure
:minify-assets
{:dev
 {:assets
   {"resources/public/css/site.min.css" "dev/resources/public/css"
    "resources/public/js/site.min.js" "dev/resources/public/js"}
  :options {:optimization :none}}
 :release
 {:assets
   {"resources/public/css/site.min.css" "dev/resources/public/css"
    "resources/public/js/site.min.js" "dev/resources/public/js"}
  :options {:optimization :advanced}}}
```

```
lein minify-assets dev
```

Defaults to `:dev` when no profile is specified.

The minifier also supports watching for file changes on JDK 1.7+, this can be enabled by running:

```
lein minify-assets watch
```

When using profiles, you'll also need to specify the profile to watch:

```
lein minify-assets watch dev
```

The minifier can also be added as a hook and will minify assets during the compile step.

```clojure
:hooks [minify-assets.plugin/hooks]
```

## License

Copyright © 2014 Yogthos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
