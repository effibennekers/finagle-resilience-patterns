package org.effiandeggie.finagle.filters

import com.twitter.finagle._
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.Address.Inet
import com.twitter.util.Future


class HostAndPortFilter(val address: Address) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
      .rescue({
        case _ => {
          Future(Response(Status.InternalServerError))
        }
      })
      .map(resp => {
        val (host, port) = address match {
          case x: Inet => (x.addr.getHostName, x.addr.getPort)
          case _ => ("Unknown", -1)
        }
        resp.headerMap.add("Host", host).add("Port", port.toString)
        resp
      })
  }
}
