import org.scardf._
import scala.xml.{NodeSeq, Elem, Text}
import com.hp.hpl.jena.sparql.vocabulary.{FOAF => jFOAF}
object FOAF extends Vocabulary( jFOAF.getURI ) {
//  val Person = wRes( jFOAF.Person )

  val List(name, givenname, knows) = 
      List("name", "givenname", "knows").map(prop)

}

    
object Temp {
val List(leif, john, bill) = List("http://leif.com", "http://john.com", "http://bill.com").map(UriRef(_))
val g = Graph(leif -FOAF.knows-> ObjSet(bill, john))

val knowsTemp =
 <p>
     <a rel="http://xmlns.com/foaf/0.1/knows" href=""/>
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

    def curse(rel:Option[String], subject:String)(n:scala.xml.Node):NodeSeq = n match {
        //case Elem(prefix, label, attributes, scope, children @ _*) => {
        case e:Elem => {
            val newRel = e.attribute("rel") match {
                case None => rel
                case something => something.map(_.text)
            } //.map(_.text).getOrElse(rel) 
            val property = e.attribute("property")
            List("resource", "href", "src").map(e.attribute).flatten match {
                case List(ref) => (g/UriRef(subject)/UriRef(newRel) map ((s)=>curse(None, s.asInstanceOf[UriRef].uri)(e)).toSeq.flatten // RDF:[subject.get(newRel)].map(curse(None, _)(e))
            //val rel = attributes.get("rel").map(_.text)
            val property = e.attribute("property")
            */
            val newSubject = List("resource", "href", "src").map(e.attribute).flatten match {
                case List(ref) => ref text // subject[rel].foreach yield Elem(null, label, attributes, scope, processedKids...)  e.g. map curse self
                case List() => subject
                case other => error("Duplicate resources for "+e.label+" element: " + other.mkString(", "))
            }
            println(newSubject)
            //currentSubject = e.attribute("
            val processedChildren = e.child.flatMap(curse(newRel, subject))
            val templated = property match {
                case Some(prop) =>  prop ++ processedChildren
                //case Some(prop) => Text(prop text) +: processedChildren
                case None => processedChildren
            }
            //Elem(null, label, attributes, scope, templated : _*) 
            Elem(null, e.label, e.attributes, e.scope, property.getOrElse(Nil) ++ processedChildren : _*) 
        }
        case t:Text => t //Text("yes") //substitute {}'s
    }

    def template(n:scala.xml.Node)(subject:String):Node = {
        
        
        curse(None, subject
    }

    def repeat
}

// vim: set ts=4 sw=4 et:
