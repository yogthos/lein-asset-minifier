# lein-asset-minifier

Lein-asset-minifier is a [Leiningen](https://github.com/technomancy/leiningen) plugin for CSS/JavaScript asset minification. The plugin uses the [asset-minifier](https://github.com/yogthos/asset-minifier) to do minification.

## Usage

To use `lein-asset-minifier`, add it as a plugin to your `project.clj` file:

[![Clojars Project](http://clojars.org/lein-asset-minifier/latest-version.svg)](https://clojars.org/lein-asset-minifier)

Then add a new `:minify-assets` key to your `project.clj` file that contains a vector of configuration items.

Each configuration item is a vector, where the first element is type:

* `:html`
* `:css`
* `:js`

The second element is a map with two required keys: `:source` and `:target`. The third is optional `:opts`, you can use it to pass compiler specific options.

The source can be either a filename, a directory, or a vector containing
a mix of files and directories. But target is always a string, target filename or target directory.

```clojure
:minify-assets [[:html {:source "dev/resource/html" :target "dev/minified/html"}]
                [:css {:source "dev/resources/css" :target "dev/minified/css/styles.min.css"}]
                [:js {:source ["dev/res/js1", "dev/res/js2"] :target "dev/minified/js/script.min.js"}]]
```

The minifier also takes optional minification hints:

```clojure
:minify-assets [[:html {:source "html" :target "html" :opts {:remove-http-protocol false}]]
```

For html configuration options please see [clj-html-compressor](https://github.com/Atsman/clj-html-compressor)

Js configuration options:

* `:optimizations` - specifies the level of JavaScript optimizations, valid values are `:none`, `:simple`, `:whitespace` or `:advanced`, defaults to `:simple`
* `:externs` - can be used to specify the externs file to be used with the advanced optimisations to prevent munging of external functions

Css configuration options:

* `:linebreak` - specifies optional linebreak for CSS resources

The plugin can be now be invoked by running:

```
lein minify-assets

```

The minifier also supports watching for file changes on JDK 1.7+, this can be enabled by running:

```
lein minify-assets watch
```

The minifier can also be added as a hook and will minify assets during the compile step.

```clojure
:hooks [minify-assets.plugin/hooks]
```

If you want to use different profiles, refer to [leiningen profile feature](https://github.com/technomancy/leiningen/blob/master/doc/PROFILES.md)

```clojure
{:profiles {:dev {:minify-assets [[:html {:source "source" :target "target"}]]}}}
```

Then run minification with next command.

`lein with-profile dev minifyassets`

## License

Copyright © 2017 Yogthos

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
