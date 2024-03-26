# vespa-yql-plugin

IntelliJ plugin adding support for  [Vespa AI](https://github.com/vespa-engine/vespa) 
[YQL request](https://docs.vespa.ai/en/reference/query-api-reference.html) files.

## Features

<p align="center">
<img src="assets/vespa-yql-plugin.gif" width=75%>
<br/>
</p>

* Run YQL requests against your vespa clusters and present the result in a table.

* Simple highlighting of the request and the yql query value.

* Simple completion support is given for request snippets and the YQL query string.

* Render a tree view of any trace in the YQL response.
  * Optionally render a zipkin view of your trace.

## Dependencies

[Vespa cluster running](https://docs.vespa.ai/en/getting-started.html) with the config 
and query endpoints available (ports 19071, 19050 and 8080). 

Locally, you can use the scripts in [vespa-k8s-cluster](https://github.com/pehrs/vespa-k8s-cluster) to 
setup a vespa cluster locally using the [kind](https://kind.sigs.k8s.io/) Kubernetes tool.

Or use [`docker-compose`](https://docs.docker.com/compose/) and use the yaml from the [`vespa-cluster`](vespa-cluster) directory:

```shell
cd vespa-cluster
# Run detached
docker-compose -d up
# or run attached to see all logs at once
docker-compose up

# To stop just
docker-compose stop
# or
docker-compose down
```

### Zipkin (Optional)

If you wish to view traces of your Vespa queries you can start a [zipkin server](https://zipkin.io/).

```shell
# Start Zipkin server
docker run --name zipkin -d -p 9411:9411 openzipkin/zipkin

# Stop Zipkin server
docker stop zipkin
# or to remove all data
docker rm -f zipkin
```

## Build &amp; install

```shell
./gradle buildPlugin
# Result goes into build/distributions/ 
ls -l build/distributions/
total 292
-rw-rw-r-- 1 matti matti 294978 mar 25 19:38 vespa-yql-plugin-1.0.0.zip
```

Install the `vespa-yql-plugin-1.0.0.zip` plugin 
file [from disk](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk)

## TODOs (Limitations)

* When you run a yql request the first time after starting the IDE there will be no results shown.
  * Workaround: just run the query again and results will show.
* Add more tests. The test coverage is VERY low atm.
* Clean up and remove code that is no longer needed!
* NO TLS support for now. You need http access directly to your clusters query port
  * This is NOT tested. Need a local setup with TLS enabled.
* The config port is not used atm. The plan is to:
  * Support view of document counts (metrics)
  * Render a configuration view of the Vespa application(s) in the cluster.
* Improve the YQL ighlighting

## License

https://www.apache.org/licenses/LICENSE-2.0