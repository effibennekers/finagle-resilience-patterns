package org.effiandeggie.finagle.filters

import com.twitter.finagle
import com.twitter.finagle._
import com.twitter.finagle.client.Transporter
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.Stack.Role
import com.twitter.finagle.loadbalancer.LoadBalancerFactory
import com.twitter.finagle.Address.Inet
import com.twitter.util.Future

class HostFilter(val address: Address) extends SimpleFilter[Request, Response] {
  override def apply(request: Request, service: Service[Request, Response]): Future[Response] = {
    service(request)
      .rescue({
        case _ => Future(Response(Status.InternalServerError))
      })
      .map(resp => {
        resp.headerMap.add("Host", address match {
          case x:Inet => x.addr.getPort match  {
            case 8081 => "effi"
            case 8082 => "eggie"
            case port => s"""${port}"""
          }
          case _ => "unknown"

        })
        resp
      })
  }
}

object HostFilter {

  def client(): Http.Client = {
    val stackWithHost = Http.client.stack.insertAfter(LoadBalancerFactory.role, module)
    Http.client.copy(stack = stackWithHost)
  }

  def module: Stackable[ServiceFactory[Request, Response]] =

    new finagle.Stack.Module1[Transporter.EndpointAddr, ServiceFactory[Request, Response]] {
      override def make(_addr: Transporter.EndpointAddr, next: ServiceFactory[Request, Response]): ServiceFactory[Request, Response] = {
        val Transporter.EndpointAddr(addr) = _addr
        val hostFilter = new HostFilter(addr)
        hostFilter.andThen(next)
      }


      val role: Role = Role("Host")
      val description = "Add host header"

    }

}
