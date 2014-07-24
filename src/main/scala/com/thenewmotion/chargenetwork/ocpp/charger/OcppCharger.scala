package com.thenewmotion.chargenetwork.ocpp.charger

import java.net.URI
import com.thenewmotion.chargenetwork.ocpp.charger.ConnectorActor.{Another, CpProducer}
import dispatch.Http
import akka.actor.{ActorRef, Props}
import com.thenewmotion.ocpp.soap.CentralSystemClient
import com.thenewmotion.ocpp.Version
import com.thenewmotion.ocpp.messages.CentralSystem

trait OcppCharger {
  def chargerActor: ActorRef
}

class OcppSoapCharger(chargerId: String,
                  numConnectors: Int,
                  ocppVersion: Version.Value,
                  centralSystemUri: URI,
                  server: ChargerServer,
                  http: Http = new Http) extends OcppCharger {

  val client = CentralSystemClient(chargerId, ocppVersion, centralSystemUri, http, Some(server.url))
  val chargerActor = system.actorOf(Props(new ChargerActor(BosService(chargerId, client, Another), numConnectors)))
  server.actor ! ChargerServer.Register(chargerId, new ChargePointService(chargerId, chargerActor))
}

class OcppJsonCharger(chargerId: String,
                       numConnectors: Int,
                       centralSystemUri: URI,
                       cpProducer: CpProducer) extends OcppCharger {
  val client: CentralSystem = new JsonCentralSystemClient(chargerId, centralSystemUri)
  val chargerActor = system.actorOf(Props(new ChargerActor(BosService(chargerId, client, cpProducer), numConnectors)))
}

