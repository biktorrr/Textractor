
<html>
  <body><?php $towrite = $_POST; ?><?php file_put_contents("result.txt", $towrite, FILE_APPEND); ?>
    <h1>Testing oai:beeldengeluid.nl:Expressie:4213932</h1>
    <p>doc id = /354423/359340/395998/4213932
      <br>user = <?php echo $_GET["person"]; ?>
    </p>
    <hr>
    <h2>terms</h2>
    <form action="survey_3.php" method="POST">
      <table>
        <tr>
          <td>
            <b>Term</b>
          </td>
          <td align="middle">
            <b>0</b>
          </td>
          <td align="middle">
            <b>1</b>
          </td>
          <td align="middle">
            <b>2</b>
          </td>
          <td align="middle">
            <b>3</b>
          </td>
          <td align="middle">
            <b>4</b>
          </td>
        </tr>
        <tr>
          <td>sport (Onderwerpen): 
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="0">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="1">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="2">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="3">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="4">
            </td>
          </td>
        </tr>
        <tr>
          <td>sport (Onderwerpen): 
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="0">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="1">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="2">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="3">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/28181" value="4">
            </td>
          </td>
        </tr>
        <tr>
          <td>Armstrong, Lance (PersoonsNamen): 
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/78265" value="0">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/78265" value="1">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/78265" value="2">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/78265" value="3">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/78265" value="4">
            </td>
          </td>
        </tr>
        <tr>
          <td>Verbruggen, Hein (PersoonsNamen): 
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/162711" value="0">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/162711" value="1">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/162711" value="2">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/162711" value="3">
            </td>
            <td>
              <input type="radio" name="http://data.beeldengeluid.nl/gtaa/162711" value="4">
            </td>
          </td>
        </tr>
      </table>
      <input type="hidden" name="id" value="oai:beeldengeluid.nl:Expressie:4213932">
      <input type="text" name="person" value="&lt;?php echo $_POST[&quot;person&quot;]; ?&gt;">
      <input type="submit" value="next">
    </form>
    <hr>
    <p>
      <a href="survey_3.html">next</a>
    </p>
    <hr>
    <p>
      <iframe src="http://immix.beeldengeluid.nl/extranet/index.aspx?chapterid=1164&amp;filterid=974&amp;contentid=240&amp;searchID=4366198&amp;itemsOnPage=10&amp;startrow=1&amp;resultitemid=1&amp;nrofresults=733198&amp;verityid=/354423/359340/395998/4213932@expressies" height="700" width="700"/>
    </p>
  </body>
</html>
