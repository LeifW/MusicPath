import java.io.FileReader
import org.scardf._

/**
 * Created by IntelliJ IDEA.
 * User: leif
 * Date: 12/18/10
 * Time: 8:50 PM
 * To change this template use File | Settings | File Templates.
 */

object Parser {
  val sb = new StringBuilder(256)
  def parse {
  val reader = new FileReader("/tmp/triples.nt")
  var char = reader.read()

  def skipWhitespace() { while (Character.isWhitespace(char) && char != -1) char = reader.read }
  def dropUntil(c:Int) { while(char != c && char != -1) char = reader.read() }
  def takeUntil(c:Int) {
    sb.clear()
    while(char != c && char != -1) {
      sb.append(char.toChar)
      char = reader.read
    }
    //sb.deleteCharAt(sb.size - 1)
    //char = reader.read
  }
  def uriref = UriRef({
        dropUntil('<')
        char = reader.read
        takeUntil('>')
        char = reader.read
        sb.toString
      })
  while (char != -1) {
  val subject = uriref
  val predicate = uriref
  skipWhitespace
  
  val obj = char match {
    case '"' => "A String!"
    case '<' => uriref
    case other => "Wtf is "+other
  }

  dropUntil('.')

  print(subject)
  print(predicate)
  println(obj)
  //while (Character.isWhitespace(char)) reader.read
  /*
  while (char != '<') reader.read
    while (char != -1) {
    val subject = {
      while (char != '<') reader.read
      println("Found a " + char)
    }
    print(char)
    char = reader.read
    */
  }
  reader.close()
  }
}
