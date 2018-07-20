# PDF-Lettercreation

_note: project seems to live in may 2017. I cannot remember what all the code is about. I am primarly checking this into github in July 2018. I do want to preserve the PDF creational stuff. The web framework and principles might not be worth to be further watched at. It is by far not usable but does render a PDF!-). So before the code dies it is pubished into github._

Two intentions:

- everyone can create easyly create a letter and download/print/send by postal mail.
- batch letter creation by CVS, XLS, Json, XML

## How to build

```
mvn package
```

will build an executable jar in the /target folder


## Setup on a unix server Apache2

for a locally running server, e.g. on port 4711:

Settings on file ``<HOME_APACHE2>/sites-enabled/default``

```
<VirtualHost *:80>
    
    ServerName prefix.yourpublicdomainofservice.de
    ServerAlias prefix.yourpublicdomainofservice.de
    
    ProxyPreserveHost On
    ProxyRequests Off

    # setup the proxy
    <Proxy *>
        Order allow,deny
        Allow from all
    </Proxy>

    # ProxyPass / http://localhost:4711/
    # ProxyPassReverse / http://localhost:4711/

    <Location />
        ProxyPass http://localhost:4711/
        ProxyPassReverse http://localhost:4711/
    </Location>

</VirtualHost>


