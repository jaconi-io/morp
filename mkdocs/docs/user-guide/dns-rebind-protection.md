# DNS Rebind Protection

If you are having troubles accessing [http://localtest.me](http://localtest.me), check if your router does DNS rebind
protection. There are various options to avoid this problem.

## Add Exception

You can add an exception for the following domains in your routers configuration:

```
bar.localtest.me
foo.localtest.me
keycloak.localtest.me
```

For instructions see your router vendors documentation. For example:

* [AVM FRITZ!Box](https://en.avm.de/service/knowledge-base/dok/FRITZ-Box-6660-Cable/3565_FRITZ-Box-reports-Your-FRITZ-Box-s-DNS-rebind-protection-rejected-your-query-for-reasons-of-security/)
* [Google Nest](https://support.google.com/googlenest/answer/9144137)

## Use external DNS

Use an external DNS service. For example:

* [Cloudflare](https://1.1.1.1)
* [Google](https://developers.google.com/speed/public-dns/docs/using)

## Add to `/etc/hosts`

Add this to your `/etc/hosts`:

```
127.0.0.1 bar.localtest.me
127.0.0.1 foo.localtest.me
127.0.0.1 keycloak.localtest.me
```
