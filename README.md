# stubby

Stubbed out version of the provenance API that's available internally at iPlant.

# What this provides
This provides an easy-ish to run version of the provenance API, but only for development purposes. This should never be run on a production system. If you run this in production and then complain about it, I will laugh at you.

Provenance information is not preserved across restarts. All write operations (i.e. calls to /register or /provenance) are synchronized at some level using Clojure's STM support, so don't expect awesome performance under high load. Applications that do a large number of writes to the provenance service need to be careful when using Stubby because it stores all information in memory.

This really, truly is a tool for development purposes only.

# To run this:

* Install Leiningen 2.
* Check out this project.
* Run 'lein deps' from this project's top-level directory.
* Run 'lein ring server' from the same directory.

# For the API, see the docs for the 1.0 verision of the provenance API.
