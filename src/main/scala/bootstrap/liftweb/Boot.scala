package bootstrap.liftweb

import java.sql.{Connection, DriverManager}

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.http._
import _root_.net.liftweb.mapper.{DB, ConnectionIdentifier, ConnectionManager, DefaultConnectionIdentifier, Schemifier}
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import Helpers._

import com.sample.model.Item

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  */
class Boot {
    def boot {
        DB.defineConnectionManager(DefaultConnectionIdentifier, DefaultDBVendor)
        Schemifier.schemify(true, Log.infoF _, Item)

        // where to search snippet
        LiftRules.addToPackages("com.sample")

        // List items menu
        val itemsMenu = Menu(Loc("Items", "items" :: "index" :: Nil, "List items"))
 
        // Build SiteMap
        val entries = Menu(Loc("Home", List("index"), "Home")) :: itemsMenu :: Nil


        LiftRules.setSiteMap(SiteMap(entries:_*))
  }
}

object DefaultDBVendor extends ConnectionManager {
    def newConnection(name: ConnectionIdentifier): Box[Connection] = {
        try {
            val connectionUrl = Props.get("db.url").openOr("") + "?user=" +
                                Props.get("db.username").openOr("") + "&password=" +
                                Props.get("db.password").openOr("")

            Class.forName(Props.get("db.driver").open_!)
            val dm = DriverManager.getConnection(connectionUrl)
            return Full(dm)
        }
        catch {
            case e: Exception => {
                Log.error(e.getMessage);
                return Empty
            }
        }
    }

    def releaseConnection(conn: Connection) = conn.close
}

