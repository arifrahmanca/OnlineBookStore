# This is an e-commerce website for a online book store
<h3>About the online book store:</h3>
<ul><li>Any customer (user) can browse books, view book details, and rate/review the book</li>
  <li>Customer can login, logout, and register to the site</li>
  <li>Customer can add books in the cart and purchase books</li>
  </ul>
<h3>How to install:</h3>
<ul><li>Download <b>OnlineBookStore.war</b> file and import it in eclipse IDE as war import</li>
  <li>Set the targeted runtime to <b>Apache Tomcat v*.*</b></li>
  <li>Set up database using the <b>SCHEMA</b> files</li>
  <li>Configure <b>context.xml</b> in the <b>META-INF</b> folder under <b>WebContent</b></li>
  <li><b>Run</b> the program in a local server under Apache Tomcat</li>
 </ul>
<h3>Rest Services:</h3>
The project provides two rest services:
<ul><li>Provides the detailed information for a book as a JSON file</li>
  <li>Return a list of all orders by book id in a JSON file</li>
</ul>
 <h3>How to access <b>Rest Services:</b></h3>
  For book info: http://localhost:8080/OnlineBookStore/rest/product/read?productId=bookID <br/>
  For order: http://localhost:8080/OnlineBookStore/rest/order/getOrder?partNumber=bookID 
