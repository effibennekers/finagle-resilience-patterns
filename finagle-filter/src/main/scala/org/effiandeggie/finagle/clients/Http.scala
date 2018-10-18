package org.effiandeggie.finagle.clients

import com.twitter.finagle
import com.twitter.finagle.{Http => FinagleHttp, _}
import com.twitter.finagle.client.Transporter
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.Stack.Role
import com.twitter.finagle.loadbalancer.LoadBalancerFactory
import org.effiandeggie.finagle.filters.HostAndPortFilter


/**
 * This Http client adds a [
 */
object Http {

  def client(): FinagleHttp.Client = {
    val stackWithHost = FinagleHttp.client.stack.insertAfter(LoadBalancerFactory.role, module)
    FinagleHttp.client.copy(stack = stackWithHost)
  }


  val role: Role = Role("Host and port Header")

  def module: Stackable[ServiceFactory[Request, Response]] =

    new finagle.Stack.Module1[Transporter.EndpointAddr, ServiceFactory[Request, Response]] {
      override def make(_addr: Transporter.EndpointAddr, next: ServiceFactory[Request, Response]): ServiceFactory[Request, Response] = {
        val Transporter.EndpointAddr(addr) = _addr
        val hostAndPortFilter = new HostAndPortFilter(addr)
        hostAndPortFilter.andThen(next)
      }

      val description = "Add host and port headers"

      override def role: Role = Http.role
    }
}
