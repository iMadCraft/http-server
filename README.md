# TD;LR

This project is just a demo server. It's not suppose to be in use! 
The code can be copied and reused under the license. Read the
<a href="./LICENSE">LICENSE</a> file. 

## Build

<pre>
$ ./configure
$ make
</pre>

## Run

<pre>
$ java -jar bin/http-server.jar
</pre>


## Demo

Command:

<code>$ curl -i http://localhost:8081/index.html </code>

Output:

<pre>
HTTP/1.1 200 OK
Content-Type: text/html
Server: Demo Server
Date: Fri, 13 Oct 2023 15:28:33 CET
Content-Length: 84

&lt;html>
&lt;body>
    &lt;h1>Hello world&lt;/h1>
    &lt;p>This is just text&lt;/p>
&lt;/body>
&lt;/html>
</pre>

Command:

<code>$ curl -i http://localhost:8081/test.html </code>

Output:

<pre>
HTTP/1.1 404 Not Found
Content-Type: text/html
Server: Demo Server
Date: Fri, 13 Oct 2023 15:34:31 CET
Content-Length: 63

&lt;html>
&lt;body>
    &lt;h1>Page Not Found: 404&lt;/h1>
&lt;/body>
&lt;/html>
</pre>
