import org.scardf._
import scala.xml.{NodeSeq, Elem, Text, NamespaceBinding, UnprefixedAttribute}
import java.net.URI
import com.hp.hpl.jena.sparql.vocabulary.{FOAF => jFOAF}
object FOAF extends Vocabulary( jFOAF.getURI ) {
//  val Person = wRes( jFOAF.Person )

  val List(name, givenname, knows) = 
      List("name", "givenname", "knows") map prop

}

    
object Temp {
val List(leif, john, bill) = List("http://leif.com", "http://john.com", "http://bill.com").map(UriRef(_))
val g = Graph(leif -FOAF.knows-> ObjSet(bill, john))
//val g = Graph(leif -FOAF.knows-> ObjSet(bill-FOAF.name->"Bill", john-FOAF.name->"John"))

val knowsTemp =
 <p xmlns:foaf="http://xmlns.com/foaf/0.1/">
     <a rel="foaf:knows" href=""/>
 </p>

val doc1 = 
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:mp="http://musicpath.org" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#">
  <head>
    <title>Cull</title>
  </head>
  <body>
    <div rel="foaf:primaryTopic" resource="#band">
      <h2 property="foaf:name">Cull</h2>
      <ul rel="mp:by">
        <li resource="/stints/ansel_cull">
          <span>
		  <a rel="mp:by" href="http://musicpath.org/people/ansel"><span property="foaf:givenName">Ansel</span></a>
		  <span rel="mp:plays" resource="/instruments/drums"><span property="rdfs:label"> on Drums</span></span>
          </span>
        </li>
        <li resource="/stints/melissa_cull">
          <span>
		  <a rel="mp:by" href="http://musicpath.org/people/melissa"><span property="foaf:givenName">Melissa</span></a>
            <span> on Guitar</span>
          </span>
        </li>
        <li>
          <span>
            <a href="http://musicpath.org/people/dylan">Dylan</a>
            <span> on Guitar</span>
          </span>
        </li>
        <li/>
      </ul>
      <div rel="mp:related">
        <h4>Related bands:</h4>
        <p>
          <div>
            <a href="http://musicpath.org/bands/sick_sick_sister">Sick Sick Sister</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/ootg">Order of the Gash</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/sickie_sickie">Sickie Sickie</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/sei_hexe">Sei Hexe</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/anonremora">Anon Remora</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/honduran">Honduran</a>
          </div>
          <div>
            <a href="http://musicpath.org/bands/slamdunk">Slam Dunk</a>
          </div>
        </p>
      </div>
    </div>
  </body>
</html>

val doc2 = 
<div rel="foaf:primaryTopic" resource="#me">
  <p property="foaf:givenName"/>
  <p property="foaf:familyName"/>
  <p>
    <a rel="foaf:mbox" href="mailto:">{{removeProtocal(foaf:mbox)}}</a>
  </p>
  <p rel="foaf:holdsAccount">
      <span typeof="foaf:OnlineAccount"></span>
  </p>
</div>
    val start = "http://musicpath.org/people/leif"
    def propertize(e:Elem)(subject:UriRef):Elem = propertize(e, None)(subject) 
    def propertize(e:Elem, rel:Option[UriRef])(subject:UriRef):Elem = {
        val processedChildren = e.child flatMap processLinks(subject, rel) 
        val templated = e.attribute("property") match {
            //case Some(prop) =>  prop ++ processedChildren
            case Some(prop) => Text(prop.text) +: processedChildren
            case None => processedChildren
        }
        e.copy(child=templated) //Elem(null, e.label, e.attributes, e.scope, templated : _*) 
    }
            //Elem(null, label, attributes, scope, templated : _*) 

    //def attr2UriRef(implicit scope:Scope)(attr:Attribute):UriRef
    def realizeLink(e:Elem, attr:String)(node:Node) = {
        val subject = node.asInstanceOf[UriRef]
        //propertize(Elem(null, e.label, e.attributes, e.scope, e.child : _*))(s)
        val attributes = new UnprefixedAttribute(attr, subject.uri, e.attributes.remove(attr))
        propertize(e.copy(attributes = attributes))(subject) 
    }

    def resolve(qname:String, scope:NamespaceBinding) = {
        val Array(prefix, local) = qname split ':'
        scope.getURI(prefix) + local
    }

    def processLinks(subject:UriRef, rel:Option[UriRef])(node:scala.xml.Node):NodeSeq = node match {
        //case Elem(prefix, label, attributes, scope, children @ _*) => {
        case e:Elem => {
            val currentRel = e.attribute("rel") match {
                case Some(r) => Some(UriRef(resolve(r.text, e.scope)))
                case None => rel
            } 
            currentRel match {
                case Some(rel) => {
                    val atts = e.attributes.map(_.key).toSet
                    // Take the first link attribute name found: 
                    List("resource", "href", "src").dropWhile(!atts.contains(_)).headOption match {
                    //List("resource", "href", "src").map(e.attribute).flatten match {
                        //case List(ref) => (g/subject/rel map ((s)=>propertize(e)(s.asInstanceOf[UriRef]))).toSeq.flatten 
                        case Some(ref) => (g/subject/rel map realizeLink(e, ref)).toSeq.flatten 
                        case None => propertize(e, Some(rel))(subject)
                        //case other => error("Duplicate resource links for "+e.label+" element: "+other.mkString(", "))
                    }
                }
                case None => propertize(e)(subject)
            }
        }
        case t:Text => t //Text("yes") //substitute {}'s
    }
/*
    def template(n:scala.xml.Node)(subject:String):Node = {
        
        
        curse(None, subject
    }

    def repeat
    */
}

// vim: set ts=4 sw=4 et:
