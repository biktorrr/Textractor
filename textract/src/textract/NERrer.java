package textract;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.json.simple.JSONArray;

public class NERrer {

	//default minimun Elasticsearch score for NE matching 
	double minScore = 3.5;
	
	// class to perform Named Entity Recognition on a String. 
	public NERrer() {
	}

   
   // super-method, get named entity objects, including matching gtaa concepts
	public ArrayList<NamedEntity> getGTAANES (ElasticGTAASearcher gtaaES, String inputString) throws IOException, DocumentException{
		System.out.print(" Recognizing Named Entities..");
		TermFinder tf = new TermFinder();
		ArrayList<NamedEntity> result = getNamedEntitiesFromCLTL(inputString);
		
		if (result.size()>0){
			System.out.println("..some found.");
			for(int i=0;i<result.size();i++){
				NamedEntity ne = result.get(i);
				JSONArray matches = new JSONArray();
				if (ne.neClass.equalsIgnoreCase("PERSON")) {
					matches = tf.matchPersonNames(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass.equalsIgnoreCase("LOCATION")) {
					matches= tf.matchGeo(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass.equalsIgnoreCase("ORGANIZATION")) {
					matches= tf.matchNames(ne.neString, gtaaES, minScore); 
				}
				else if (ne.neClass=="MISC") {
					matches= tf.matchNames(ne.neString, gtaaES, minScore);
					matches.addAll(tf.matchPersonNames(ne.neString, gtaaES, minScore));
					matches.addAll(tf.matchOnderwerpen(ne.neString, gtaaES, minScore));
				}
				ne.gtaaMatches = matches;
			}
			
		}
		else {
			System.out.println("..none found.");}
		if (Textractor.debug==true){System.out.println("\nNER result: "+ result);}
		return result;
	}
	

	
	
	
	//get Named Entities from CLTL web services
	public ArrayList<NamedEntity> getNamedEntitiesFromCLTL( String inputString) throws IOException, DocumentException{
		ArrayList<NamedEntity> result = new ArrayList<NamedEntity>();

			String kafResult = getTreeKafFromCLTL(inputString);
			String nerResult = getNerResultFromCLTL(kafResult);
			if (nerResult.length()>0){
			
				Document document = DocumentHelper.parseText(nerResult);
				List list = document.selectNodes("//entity");
				
				for (int i=0;i<list.size();i++){
					NamedEntity ne = new NamedEntity();
					Element entity = (Element) list.get(i);
					ne.neClass = entity.attributeValue("type");
					String neString = "";
					Element refs =  entity.element("references");
					for (Iterator it = refs.nodeIterator(); it.hasNext();){
						Node node = (Node) it.next();
						if (node.getNodeType()==Node.COMMENT_NODE){
							neString = node.getText();					
						}
					}
					ne.neString = neString;
					result.add(ne);
				}
				
			}
			else{
				System.out.println("empty NER");
			}
		return result;
	}
	
	private static String cleanNerResult(String in) {
		return "<?xml version" + in.split("<?xml version")[1];

	}


	private String getNerResultFromCLTL(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/opener_ner?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();		
	}

	// get the KAF representation of a string (uses webservice)
	private String getKafFromCLTL(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/opener_tokenizer?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();
	}
	
	// get the KAF Treetag representation of a string (uses webservice)
		private String getTreeKafFromCLTL(String inputString) throws IOException {
			String urlString = "http://ic.vupr.nl:8081/treetagger_plain_to_kaf?lang=nl";
			// Connect to google.com
	        URL url = new URL(urlString);
	        String postData = inputString;
	        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	        connection.setDoOutput(true);
	        connection.setRequestMethod("POST");
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
	         
	        // Write data
	        OutputStream os = connection.getOutputStream();
	        os.write(postData.getBytes());
	         
	        // Read response
	        StringBuilder responseSB = new StringBuilder();
	        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	          
	        String line;
	        while ( (line = br.readLine()) != null)
	            responseSB.append(line);
	                 
	        // Close streams
	        br.close();
	        os.close();
	         
	        return responseSB.toString();
		}
	
	// get the NER representation of a string (uses webservice)
	private String getBasicPipeline(String inputString) throws IOException {
		String urlString = "http://ic.vupr.nl:8081/pipeline_basic?lang=nl";
		// Connect to google.com
        URL url = new URL(urlString);
        String postData = inputString;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
         
        // Write data
        OutputStream os = connection.getOutputStream();
        os.write(postData.getBytes());
         
        // Read response
        StringBuilder responseSB = new StringBuilder();
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
          
        String line;
        while ( (line = br.readLine()) != null)
            responseSB.append(line);
                 
        // Close streams
        br.close();
        os.close();
         
        return responseSB.toString();
	}
	
	

	public static void main(String[] args) {
		
		
		NERrer gogo = new NERrer();
		String testEx1 = "888Als klein meisje wil Chantal Janzen dansen in Het Zwanenmeer.Maar als ze op haar twaalfde haar eerste musical ziet, weet ze:Dit is wat ik eigenlijk wil.Zingen, dansen, toneelspelen.Ze laat dit Joop van den Ende weten in een brief.Maar daar kreeg ze nooit antwoord op.Jaren later, eenmaal in Amsterdam, valt ze hem wel op.En krijgt ze hoofdrollen in grote musicals als:Tarzan, Wicked, Petticoat.Ze presenteert talloze tv-programma's, speelt in films...en op dit moment schittert ze als Rachel Hazes in 'Hij gelooft in mij'.*Op wie ik heel mijn verder leven bouw*Ik geloof in jou*Ik geloooof in jouNaast dit alles is ze ook nog moeder van de kleine James...en al jaren gelukkig met haar grote jeugdliefde uit Tegelen, Marco.Chantal, wat fijn dat je meedoet. Dank je.In je enorme drukke bestaan.Je ontvangt ons hier eigenlijk in je tweede huis, he?Ja, hier ben ik echt 4-5 keer per week te vinden, in het DeLaMar.Want er gaan drie kunstenaars heel erg naar je zitten kijken vandaag.Heb je je daarop voorbereid?Daar kan je je eigenlijk niet op voorbereiden, he?Ik... kan heel goed stilzitten.En ook niks zeggen. Kan ik ook heel goed.Ik ben meer een flatliner dan altijd veel energie hebben.Wat is een flatliner?Ik ben vrij constant, op het saaie af.Op het saaie af.Vind ik zelf.Als ik op een verjaardag ben, kunnen ze zeggen: Goh, was ze er wel?Vind ik prima.Hahaha! Ik heb er de energie niet voor.Ik zal nooit binnenkomen en zeggen: Hallo, daar ben ik, mensen. Nee.Ik verheug me enorm op deze lange, saaie dag, Chantal.Jaha! Ik ook!Er staat de kunstenaars dus een lange saaie dag te wachten.Hopelijk lukt het hun wel een sprankelend portret neer te zetten.In het tweede huis van Chantal mogen ze een eerste opzet maken.Alleen weten zij nog niet van wie.Van Lisa van Noorden kunnen we een uitzonderlijk portret verwachten.Met stift, pen of potlood maakt ze verschillende portretten...die ze op de computer bewerkt. En daar komt een uniek object uit.Roos van der Vliet schildert zeer gedetailleerd portretten...van vooral jonge vrouwen.Door een structuur aan te brengen en met een typerende kleur...krijgen deze vrouwen een bepaalde zachtheid over zich.Kunstenaar Martin-Jan van Santen pakt het anders aan.Met een grove toets laat hij zijn portretten spreken.Kleur en licht zijn erg belangrijk voor hem.Maar hij laat zich vooral leiden door schoonheid, romantiek en sfeer.Hier staan de sterren van het doek, de kunstenaars.Zijn jullie zenuwachtig? Martin-Jan?Ja...Ja? Waarom?Eh...Ik doe dit nooit en straks kijken een heleboel mensen mee...hoe ik schilder. Dat vind ik niet zo leuk.Daar hou ik niet zo van. Maar je doet wel mee?Ja, dat wel. Ik wil graag een keer iemand schilderen...die ik al jaren van tv ken.Vind ik erg leuk.Hoop je. Hoop ik, ja.Wie het eigenlijk is, is van dit gesprek de kern ongeveer.Ja.Heb je enig idee?Het belangrijkste is dat hij of zij sterretjes in de ogen heeft.Heb je een voorkeur voor een soort model?Eigenlijk niet. Nee.Ik vind het ook een uitdaging om te kijken of wat ik doe...of ik dat kwijt kan en past op wat er ook voor me staat.Jaja.Ja.En heb jij je gedachten laten gaan over een eventuele ster?Ik hoop Chantal Janzen.Hm.Waarom?Nou, omdat ik ook wel vaak...Ik schilder eigenlijk altijd vrouwen, jonge vrouwen meestal.En zij heeft wel een mooi poppengezichtje.En daar zou ik heel blij van worden. Een poppengezichtje?Ja.Weet je, we kunnen heel lang en heel kort hierover praten.We kunnen het best vragen of de gast binnenkomt.En dan zeg ik altijd: Laat de gast maar binnenkomen.Hallo samen. Wat goed van jou.ZE STELLEN ZICH AAN ELKAAR VOORNou, daar ben ik dan, het object. Ik ben er heel blij mee.Valt het mee? Goed zo.Super. Dit is je stoel, Chantal.Denk aan hele leuke dingen. Ik wens je ontzettend veel plezier.Dank je.Nou... Jeetje.Ja...En nu?Spannend.En nu?Nou, pak je kwast of waar je het mee doet. Ik weet het niet.Alright.MARTIN-JAN:Ik zit te denken, kun je je benen anders kruisen?Tuurlijk.Vind ik iets mooier. Of niet?Die benen komen er bij mij niet op. Bij mij ook niet.Haha!ZE PRATEN DOOR ELKAARWat jammer. Nog nooit gehoord dat ik lange benen heb.Heb jij nog steeds dat je haar van voor...Hoe meer ze deze kant op kijkt, hoe blijer ik ben.Maar ik wil dat zeker niet eh...dat afdwingen naar jullie toe.Ik kan het makkelijker maken: Dit is mijn beste kant.Dus als je daar gewoon naar kijkt, heb je altijd een leuke...ding. Haha.Dan doen we alleen die kant en spiegelen hem daarna.Ja, haha.Oke.VROLIJK MUZIEKJETjonge, wat lastig.Wat zei je nou? Tjonge, wat lastig.O, top.Haha.Fantastisch.O ja, is dat goed? Nee, nou... Ik weet het niet.Dit wordt je eerste poseersessie?Nou, ik heb een eh...Ik heb een wassen beeld.Zo. En ja...Sommige dingen lijken heel goed.Maar ik sta met een enorme gevel in Madame Tussauds.ZE LACHEN En het erge is...Mijn moeder ging er een keer met vriendinnen naartoe...En ehm...Dan zie je ook al die toeristen die geen idee hebben...Dan gaan ze met dat beeld op de foto en altijd een hand op een tiet.Mijn moeder ging heel snel naar huis. Die kon het niet aanzien.Chantal...Ik vind jou altijd een beetje een bedrieglijk iemand.In de zin van:Je ziet er zo schattig uit, maar je bent eigenlijk best vals.CHANTAL ADEMT DIEPDat ben ik niet met je eens, Hanneke. Ik verheugde me zo hierop.Ik bedoel niet helemaal 'vals', maar je hebt iets gevaarlijks.Je kan elk moment...Een plaagstoot uitdelen, een heel snedig grapje maken.Maar dan vind ik het eerder een plaagstoot, of tenminste...Ik neem waar. En dan...zeg ik daar iets van. Maar volgens mij...Ik zit niet van tevoren van: Daar ga ik iets leuks van maken.Dat is echt gewoon... waarnemen.Misschien zelfs integendeel. Dat je achteraf denkt:Had ik niet heel even... O! Ja, zeker.Toen jij klein was...droomde je dus van dansen en iets later van musicalster worden.Toen keek je vanuit Tegelen naar deze wereld.Is die wereld zo fijn als je toen dacht?Driekwart wel. Ja.Ik had dit nooit... durven dromen.Ik wilde op een podium staan. Het maakte niet uit waar.Ik droomde ook nooit van hoofdrollen of in de schijnwerpers.Maar ik bedoel niet solo. Ik dacht altijd: Met zijn allen.Met Het Zwanenmeer en het corps de ballet.Maar ik dacht niet: Ik ben die... Die zwarte of die witte zwaan?Nee.Driekwart.Ja, dat eenkwartje... Dat is het bekend zijn.Hoe is dat dan?Ik vind het lastig.Ik vind het lastig als iemand meteen zo... op je af komt.Ik heb liever dat mensen iets tegen je zeggen dan staren.Ik voel me dan gewoon heel alienated.Ik vind dat heel...Ik wil me niet anders voelen.Ik wil blenden in de rest.Ik wil totaal niet boven iets uit steken.Op TONEEL wil ik 't bijna afdwingen. Dan moeten ze naar me kijken.Maar dan ben ik niet mezelf.Da's veiliger.Toen je aan dit hele avontuur begon...Toen je 'emigreerde' naar Amsterdam...Dat was een emigratie, he? Ja, echt. Ja. Ja.Het is een ander land, Tegelen? Limburg.Ik heb heel veel gehuild.Heel eenzaam gevoeld.Toen mijn ouders wegreden uit Amsterdam...Ehm...Maakte ik mijn koffer open en daar zat een briefje van mijn moeder...Dat heb ik nog steeds.'Op dit moment hebben we allemaal heel lang gewacht'.In de zin van: Binnenkort gaat ze op kamers.'Het zou moeilijk zijn, papa en mama zijn er altijd voor jou'.Ik heb nog steeds... Ik heb zo gehuild. Ik vond het heel moeilijk.Zijn ze er ook altijd voor je? Nog steeds?Altijd. Mijn ouders zijn er altijd.Als ik mijn ouders 's ochtends bel 'onze vaste oppas is ziek', dan zitten ze al in detrein.Ja?Ja, dat vind ik fantastisch.Praat je Limburgs met je ouders?Ja!Wij kunnen geen Nederlands met elkaar praten.Nee?Nee.Is dat niet raar eigenlijk dat je je moedertaal hier niet gebruikt?Nee, ik mis dat niet. Mijn man en ik praten thuis ook gewoon Nederlands.Wat denk je dat het geheim van jouw succes is?Volgens mij is er geen geheim. Ik denk...Willen leren, dat is wel belangrijk.Ik denk dat een bepaalde nuchterheid en realiteitszin...dat dat je ook ver brengt. En kritisch naar jezelf durven kijken.Ook mensen om je heen verzamelen... Of verzamelen... Mensen vragen:Heb je het gezien? Hoe doe ik dat? Wat vind je hiervan?Ik heb heel veel mensen om mij heen, maar weinig jaknikkers.Volgens mij heb ik er geen een. Helaas.Ze branden je allemaal af?Nou, ik kan eh...Ik moet zeggen dat ik veel mensen heb die heel eerlijk tegen me zijn.En dat is best lastig. Vaak.Maar in the end ben ik er altijd beter door geworden.RUSTIGE ELEKTRONISCHE MUZIEKGa je lekker, Lisa?Ja, het gaat best wel lekker.Wat is dit stadium? Wat doe je nu?Ik probeer voornamelijk een bepaald gevoel eigenlijk erin te krijgen.Dus het klopt nog niet qua gezichtsverhoudingen.Dat repareer ik later allemaal wel.Het gaat me eerder eigenlijk om een bepaald soort...uitstraling die ze heeft die ik probeer te vangen.Ze heeft hele donkere ogen. Dat komt natuurlijk ook door make-up.Maar er zit ook iets heel melancholieks in haar blik.En daardoor...Dat... Dat conflicteert een beetje met dat poppetje.Want jij begon er ook al over.Maar dat is een soort krachtveld met elkaar wat ik heel mooi vind.Roos, hoe is het om hier zo te werken?Ehm, heel erg lastig.Heel erg leuk ook wel.En sowieso natuurlijk fantastisch om zo...dicht bij Chantal op de huid te zitten...en zo lang te mogen kijken.Maar...Waarom is het lastig?Ja, omdat ik als kunstenaar toch wel echt een kluizenaar ben...en normaal alleen in mijn atelier zit en daar zit te verstoffen tot...Nou ja, urenlang. Ik zie het niet aan je.Dank je wel.Niet zo'n stoffig type.Normaal wel, hoor.O, oke.CHANTAL LACHT Dat valt wel mee.Maar ik hou heel erg van afzondering en dan is dit een groot contrast.Wat vind jij de lastige punten schildertechnisch?Ik vind het best wel behoorlijk...perfect.Te doen. Nou... Best wel behoorlijk perfect.Qua verhouding.Nou, een opsteker.Martin-Jan.Behalve die enorme dikke kwast waar je de hele tijd...mee slaat op dat doek, wat vind je van het model?Eigenlijk makkelijker dan ik dacht.Want ze is...Ze kwam binnen en toen dacht ik wel van: o jee...Want het is een heel mooi, gaaf gezicht.En dat is lastig...Het is vaak lastig om daar een karakteristiek in te vinden.Maar het valt mee, ik vind het leuk om te doen.Er zit meer karakter in dan je denkt?Ja.Heeft ze lastige...puntjes?Ja, de kaak is heel...raar.Ja, haha.Je denkt soms van een bepaalde hoek: Ze heeft een erge puntkin.Van de andere hoek juist niet. Dan is hij wel klein, maar heeft hij...zo'n hoekje, zo'n bakje.Ja, klopt. En dat verandert steeds.Jullie hebben alle drie een beetje of de kop of tot hier.Ga je nog de hele gestalte pakken?Misschien wel, ja. Dat vind ik wel iets.Die schoenen hebben ook een hoop geld gekost, dus misschien leuk...als die er ook op komen. Ze hoeven niet terug naar de winkel.Dat moet je terugverdienen. Dan moeten ze erop.Nou, ik zal mijn best doen. Ja. Dank je.Maar...Ik doe niet alleen je gezicht.Nee. Nee.RUSTIGE HARMONICAMUZIEKWe hebben het heel veel gehad over je werk.Even prive: Wat heeft het moederschap voor jou betekend?Dat is nog leuker en...dan ik dacht. Het is zo geweldig om een kind te zien van...Zo'n samensmelting van iemand waarvan je zielsveel houdt.Van Marco.Dan heb je samen iets gemaakt. Dat is van jullie tweeen.En alleen vooral in dat opzicht zou ik er echt wel...20 willen hebben.Ja.Omdat ik zo ook heel blij ben met mijn man.Dat je... Het is fantastisch dat je samen een mensje hebt gemaakt.Heeft de komst van James, je zoontje...registers in je opengetrokken waarvan je niet wist dat je ze had?Ja. Het heeft me sowieso veel emotioneler gemaakt.Ik ben al best wel emotioneel, maar ik ben ook ontzettend verstandig.Ik ga behoorlijk snel weer op een ding van:Oke, het is nu niet slim om nu te gaan huilen.Door hem laat ik dat wel heel veel varen.Want... Ik weet niet....In het begin zeggen ze: Dat zijn de hormonen.En later is het gebleven.Hij is al vierenhalf, he?Ja, ik kan ontzettend ontroerd raken en dat heeft het in mij losgemaakt.En ik moet meteen denken...Wat...Wat...Hij... raakt heel snel ontroerd.En dat ontroert mij weer zo dan. Hoe dan?Ik ging een liedje voor hem zingen, we lagen in bed.Ik zong eigenlijk een heel vreselijk liedje.Wat dan?*Ik hou van jou*Alleen van jouIk weet niet hoe ik daarop kwam.Ik kan niet leven in een wereld zonder jou.Nou, dan hoorde ik hem weer.Ik zeg: Liefje, gaat het? 'Ik, ik...''Ik dacht: Ik ga er lekker van slapen, maar ik moet ervan huilen.''Maar niet stom huilen, fijn huilen'.Nou, dat vind ik echt...Zo moet je eigenlijk altijd door het leven gaan.Meteen als iets je raakt: Bam, eruit.En ook als hij heel blij is, ook meteen.Maar tegelijk denk je: O, kind...Zo'n heel leven nog. Overal rijden trams.Ja.Overal is gevaar. Maakt het wel eens angstig?Ja, ik heb besloten hem nooit te leren fietsen.Dan ben ik daar niet bang voor, dat hij alleen op de fiets gaat.Dus op zijn 27e zit hij nog bij jou achterop.Of op een driewieler met zijwieltjes.Dat zou je dan het liefste willen doen, maar...Anderzijds ben ik totaal geen angstige moeder.Ik laat hem ook rustig...Als ik denk: Mwah, hij kukelt misschien over dat opstapje...Dan laat ik dat ook wel gebeuren.Eigenlijk ben jij wel al met al een zondagskind, he?Ja, ik vind mezelf ook natuurlijk geen zondagskind.Maar dat heb ik dan dubbel.Ik ga niet klagen, maar ik leg uit waarom ik dat vind.Ik heb, omdat er zo veel fantastische dingen...die bij een zondagskind horen, je overkomen...Je droomt ergens van...en mijn dromen zijn tot nu toe eigenlijk bijna allemaal uitgekomen.Daardoor zijn sommige dingen... komen dan in de verdrukking.Waar je dan niet lang bij stil kunt staan.Je kan niet lang rouwen. Dat sta ik mezelf ook niet toe.En soms ben je er dan niet genoeg voor mensen.Ik heb een tante en dat was mijn lievelingstante.Mijn andere tantes zijn ook leuk, maar die lievelingstante heb je.En die is overleden. Die was heel lang ziek, ze had kanker.Toen ik dat hoorde, ging ik ook meteen tegen mijn moeder:Waarom heb ik haar niet meer gebeld?Ik heb haar niet meer gebeld. Dat kwam eigenlijk doordat ik te veel...te druk was.Wat voor tante was dat? De zus van mijn moeder.En dat was ook voor het eerst dat mijn moeder...die altijd sterk is...Die brak helemaal. En dat zijn momenten dat je denkt: hee...Mijn ouders zijn niet voor altijd 40 jaar. Ze zijn niet altijd sterk.En ze zijn niet altijd... Zij zijn echt wel mijn basis.En ze zijn zo'n sterke basis voor mij. Ze zijn ook nog samen.Als je ziet als je ouder wordt dat ook je ouders maar mensen zijn...en niet die superhelden... Dat is wel even heftig om te zien.Wat moet jij zonder je ouders?Heel goed doorleven.Dat zijn denk ik hun woorden.RUSTIGE PIANOMUZIEKEen lange poseerdag is ten einde.De kunstenaars waren een beetje verrast dat...onder het lieve uiterlijk van Chantal...ook een grote kracht en melancholie schuilt.Ze proberen dat op het doek te krijgen.Ze maken nog wat foto's en gaan dan naar hun ateliers...om de portretten af te maken.Chantal ziet pas wat ze ervan hebben gemaakt bij de onthulling.Lisa is druk bezig met een buitengewoon portret.Ja, dit is... de buitenkant van Chantal.Het is een heel oppervlakkig kunstwerk nog.Ja.Maar hier komt straks het portret van Chantal op.EEN aan de ene kant en EEN aan de andere kant.Hetzelfde aan beide kanten?Nee, ze zijn verschillend.Deze tekeningen worden ingescand op hoge resolutie.Samen met een aantal andere onderdelen...zoals de inktvlekken die we daar zien. Dat moet er ook bij.En dan componeer ik dat tot een nieuw werk.En die komen... dat wordt een print.O, dus dit is het haar. Dit is de buitenkant.Ik snap het.Hier zie je al een beetje de vorm erin.Ja.Denk je niet dat het een handicap is om het ergens kwijt te kunnen?Nee, dat denk ik niet. Je kunt het makkelijker kwijt dan je denkt.Je hebt meestal een trapgat of hoek in een huis, een loze ruimte.En waar je heel graag je eigen kop hangt?Ja, haha.Roos gaat voor Chantals schoonheid.Dit is een portret met HELE mooie ogen, he?Vond je Chantal een mooie vrouw?Ja, zeker. Ja. Dat is echt een hele mooie vrouw.Dat is ook meteen duidelijk als je haar in het echt ziet.Dan snap je ook wat ze op tv doet, waarom ze daar is.Het is heel erg... 'in your face' zoals dat heet.Ze moet wel graag zichzelf heel groot willen zien.Was dat nog een punt voor jou? Ze is inderdaad groot in beeld.Daar heb ik over nagedacht. Ze kan het confronterend vinden.Ik wilde haar gewoon een moderne uitsnede meegeven.Ze is mooi genoeg, dus dat kan gewoon... zo.Hm.Zitten er moeilijke puntjes in haar gezicht?Ehm...Nou ja, de letterlijke puntjes, de sproetjes.Ik vond haar mond heel ingewikkeld.De glans. Ze heeft natuurlijk flink wat lipgloss op.En dat wilde ik graag laten zien.En dat is ook echt een uitdaging geweest.Bij Martin-Jan is weer een heel andere Chantal te zien.Een stoute Chantal.Ja.Heb je haar zo ervaren? Ja, nou...Misschien hield ze dat nog een beetje in op de poseersessie.Maar...Ik had wel het gevoel dat ze ondeugender is dan ze zich voordoet.Ik vroeg haar: Kun je even schalks kijken?En dat deed ze. Dus het is schalks. Een schalkse Chantal is het.En je geeft haar een lekkere houding, lekker stoer.Ja.Zo heb je haar ook ervaren?Ze is superstoer. Ze weet precies wat ze wil.O ja?Ja.Misschien is dat het stoute.Ze ziet er wel lief uit, maar...Ja, een stoere meid.Wat zijn de lastige punten in haar uiterlijk?Het is prachtig haar, maar het is ingewikkeld opgestoken.Dat vind ik echt heel lastig.Haha. En de streepjes op haar shirt.Ik word gek van die streepjes. Daar kon ze allebei iets aan doen.Haar haar niet zo raar opsteken en geen streepjes aan.Heb je bij het maken van dit portret haar in gedachten...als iemand die moet kiezen?Ik weet niet wat Chantal leuk vindt, dus ik doe gewoon mijn best.Ik kan ook niet anders.Het is weer zover.Het spannendste moment van dit programma is aangebroken: de keuze.Zo dadelijk gaat Chantal Janzen hier in Museum Rotterdam...voor de allereerste keer zien...wat de kunstenaars van hun portret hebben gemaakt.En Chantal moet kiezen, want ze mag er maar EEN mee naar huis nemen.Ik sta hier met Nicole van Dijk. Curator van Museum Rotterdam.Deze tentoonstelling heet 'Echte Rotterdammers'.Zijn dit allemaal echte Rotterdammers?Dit zijn echte Rotterdammers die van heinde en verre zijn gekomen.En die wij zijn tegenkomen bij projecten die wij doen in de stad.En heel veel van hun hebben meegewerkt aan deze tentoonstelling.Die projecten in de wijk kunnen wij doen...omdat dat door Stichting Doen mogelijk wordt gemaakt.Zij stimuleren ons ook inhoudelijk erg om samen met Rotterdammers...dat erfgoed van de stad te verzamelen.En is dit een tentoonstelling die leeft in de stad?Die belangrijk gevonden wordt?Ja, we merken het aan de reacties.Mensen zijn erg positief en blij als ze hier komen.Als je je meer verbonden voelt met een stad...doe je ook meer je best om er wat voor te doen.Er zijn weer veel prijzen gewonnen in de BankGiroLoterij.Van reischeques tot geldbedragen...maar ook cultuurprijzen en hotelarrangementen.Mevrouw Lailei uit Amsterdam opende met de laatste vijf cijfers...van haar rekeningnummer de kluis en won 10.000 euro.Omdat ze meespeelt met twee loten en verdubbelaar...won ze in totaal 40.000 euro.Lieve kunstenaars. Daar staan jullie werken.Zijn jullie zenuwachtig? Martin-Jan? Het is een geheimzinnige toestand.Dus ja.Jullie hebben pittige concurrentie, Roos?Dat klopt. We zijn wel aan elkaar gewaagd, geloof ik.Hebben de andere twee werken jou nog op een idee gebracht?Dat je denkt: o jee...Nou, ik ben nu wel extra gespannen ja, dat wel.En Lisa?Het zijn er eigenlijk twee, he? Heb je een favoriet?Eigenlijk de kant met de gesloten ogen vind ik favoriet.Omdat je...dan het beste echt naar iemand kunt kijken...zonder gestoord te worden door een confrontatie eigenlijk.Zo meteen gaat Chantal hier staan, gaat ze kiezen.En ik wil jullie vragen om ergens anders te wachten tot ze klaar is.Zodra ze het weet, horen jullie het als eerste.Oke.Tot zo.Tot zo.SPANNENDE MUZIEKChantal, dit zijn ze. Hoe vind je ze?Ik heb het idee dat ik daar hang.Dat het een soort voodoopop is of een dartbord.Ik vind dat een beetje eng uitzien eerlijk gezegd, wat daar zweeft.Kun je een dartbord gebruiken thuis? Want dan is het wel handig.Ja, of het is leuk voor mensen die een hekel aan je hebben.Die kunnen dat kopen.Zometeen zie je drie portretten.Van twee portretten moet je zeggen: 'nee.' Hoe kijk je daar tegenaan?Nou...Je ziet het eigenlijk altijd meteen aan mij.Dus wat dat betreft ben ik een bar slechte actrice.Ik kan dat niet verbergen.Dus als dat hele gezicht...Als ik het niks vind, als het gaat hangen, haha!Oke.Ja, dan zul je dat zien.Ik ga de eerste onthullen, Chantal.Oke.Komt-ie.Komt-ie.O, ik vind het heel mooi.Je klinkt verbaasd.Ja. Omdat ik het wel...Omdat ik toch wel had verwacht van: O Jezus, ben ik dat?Dit ben jij.Ja.Ja, w-w-wel anders. Best grote boezempartij nog, zie je dat?Ja, gratis en voor niks, zomaar zulke tieten.En gespierde armen. Het is wel allemaal echt.De boezem en het haar, allebei. O, nou, respect.Ja, toch.Komt er nog EEN, Chantal. Dit kan alleen maar tegenvallen.Kan alleen maar tegenvallen, of niet.Ooh!O, die vind ik ook heel mooi. Mijn vader gaat huilen als hij dit ziet.Want?Nou, het is... Ik vind het wel engelachtig.Wat natuurlijk niet echt bij me past, maar...Dit is zijn schatje? Nou... Ja, het is wel heel mooi.Ik vind mijn mond daar ook mooier dan in het echt.Dus deze twee vallen al niet tegen? Dit valt me hartstikke mee!Nou, daar hangt nog die eh...Dit ding, dat vind ik nogal wat, ja.Zal ik...Kijk uit dat je het er niet helemaal aftrekt, Hanneke.Ik weet niet wat je... Ik pas ontzettend goed op.Komt-ie.Oke...Oke...O ja, je kan het wisselen.Ja, je kan het ronddraaien ook.Goh...Goh, hoe kom je erop, denk je dan.Ja, het is haar stijl.Ja.Aah, haar stijl. Dan heb je al iets verraden.Goh, wat grappig.Ik zou zeggen: Ga er heel dichtbij. Neem je tijd.En veel plezier. Want jij moet kiezen.Aan het haar zie je wel dat ik het ben, en de contouren. Maar...als je het haar eraf haalt...wat op zich al gek is, als je het zou doen.Dan zie je eigenlijk niet wie het is.Ja, dit is wel echt eh...Ja, dat vind ik wel echt heel mooi, hoor.Maar ik neem niet aan dat dit helemaal...geschilderd is.Dat lijkt wel... trucage.Als ik naar deze kijk, denk ik: Diegene vindt me wel heel aardig.Of die moet mij wel heel leuk vinden, denk ik dan.En nu komen we bij... dit. Ja, jeetje.Ja, ja... Ik vind het ook wel weer heel lief.Maar dat is ook wel zoiets, he?Of je dat nou gaat ophangen, he?Dan denk ik: Ja...Wil ik dit nou in mijn huis hebben, zo groot, prominent?Ik ben niet zo iemand die zichzelf groot in de woonkamer heeft hangen.Maar ik ben er wel uit.Kiest Chantal voor het spannende portret van Martin-Jan?Het lieve werk van Roos?Of het aparte object van Lisa?Chantal, het was niet makkelijk, he? Nee.Vertel even wat je van alle drie vindt, nog los van je keuze.Ja, deze zag ik als eerste.Ik zei: O, dat vind ik echt heel mooi. Ja.En toen dacht ik: Dan kan die alleen maar tegenvallen omdat ik die mooi vond.Maar toen vond ik die ook heel mooi.En bij die zei ik: hee... da's gek. Iets in die strekking. Toch? Ja.Maar je hebt uiteindelijk gekozen? Ik ben er uit gekomen, ja.Eh...Zou je mij kunnen zeggen...welk schilderij, in willekeurige volgorde, je niet naar huis neemt.Ja, dat kan ik zeggen en dat is die helaas.Omdat ik hem heel graag aan mijn ouders wil geven.Maar ik denk dat mijn ouders wat traditioneler zijn.Ik weet niet of dit Lisa erg troost, maar hij gaat in elk geval niet mee.God ja, ik hoop dat het een beetje troost.Hebben we er nog twee over.Ja.Ik zou ze het liefst in een blender gooien, deze twee.En dan komt daar het beste schilderij uit.Wat van wat dan?Die houding vind ik sterk. Ik vind dat haar echt ZO ontzettend knap.Van veraf vind ik hem het mooist. Als ik dichterbij kijk... Eh...Dan denk ik... Goh...Gek, sommige dingen.Haha.En die boezem is ook fijn. Ja, die boezem vond je fijn.En bij dit had ik echt ehm...Ja, dat lijkt gewoon...niet geschilderd.Dat vind ik er heel mooi aan. Dat vind ik echt heel knap.Wat mag ik hieruit opmaken, eigenlijk?Dat je wat niet meeneemt en wat wel?Ik kies voor de ogen.Nou, je kiest voor Roos.Ja.Ik kies voor Roos. Krijg ik Roos er dan bij?Als je wil, dat moet je onderhandelen met Roos en Marco.Roos zet ik iedere dag ernaast! Ja.Geef ze allemaal een hug.Roos, je hebt gewonnen! Ga bij elkaar staan.Echt HEEL knap.Super. Heel knap, heel mooi.Een hele tijd later. Chantal had het best moeilijk met de keuze.Het ene vond ze heel interessant. Het andere vond ze leuk qua houding.Maar ze heeft uiteindelijk gekozen voor het engelachtige portret van Roos...dat ze zeker aan haar ouders in Tegelen gaat geven.En die andere twee schilderijen worden geveild voor een goed doel.En hoe dat precies gaat? Dat ziet u op onze website.NPO ONDERTITELING TT888, 2013 tt888reacties(a)omroep.nl";
		String test1 = "De Nederlandse economie is in het tweede kwartaal harder gegroeid dan eerder werd gedacht. Economie in tweede kwartaal harder gegroeid dan gedacht Foto:  Hollandse Hoogte Dat blijkt woensdag uit een tweede raming van het Centraal Bureau voor de Statistiek (CBS). De economie groeide met 0,7 procent ten opzichte van het eerste kwartaal. In augustus werd er nog vanuit gegaan dat de economie met 0,5 procent was gegroeid. Uit de tweede raming blijkt dat consumenten iets meer hebben uitgegeven dan eerder was berekend. Ook is er meer geïnvesteerd in zogenoemde vaste activa. Dat zijn dingen die langer dan een jaar worden gebruikt zoals gebouwen, machines en software. Het statistiekbureau past het beeld van de economie overigens niet aan. De groei van het tweede kwartaal is nog steeds vooral aan de export toe te schrijven.  Jaar op jaar Ook de groei van jaar op jaar was groter dan eerder gemeld. Volgens de nieuwe gegevens is de economie met 1,1 procent gegroeid ten opzichte van het tweede kwartaal in 2013. Eerder werd een groei van 0,9 procent berekend. Dat verschil ligt vooral in de consumptie van huishoudens en investeringen in bouwwerken. Een aantal percentages viel echter lager uit. Dat gold onder meer voor het handelssaldo, dat is de export minus de import. Ook heeft de overheid minder uitgegeven dan eerder was berekend. Beweeg de cursor over de lijn om de percentages te zien. De gegevens zijn afkomstig van het CBS. - (c)NU.nl/Jerry Vermanen Verder heeft het CBS de gegevens over banen ook aangepast. Er waren in het tweede kwartaal duizend minder banen dan in het voorgaande kwartaal. De eerste raming ging uit van een daling van achtduizend banen. Bij de vergelijking van jaar op jaar is de daling bijgesteld van 80.000 naar 70.000 banen. Informatie De eerste raming is 45 dagen na afloop van het tweede kwartaal berekend. Maar daarna kwam er meer informatie binnen van bedrijven in de zakelijke dienstverlening, de bouw en de horeca.In de afgelopen vijf jaar weken tweede ramingen gemiddeld 0,06 procentpunt af van de aanvankelijk gepubliceerde gegevens.";
		String test = "Hallo, mijn naam is Victor de Boer en ik woon in de mooie stad Haarlem. Ik werk nu bij het Nederlands Instituut voor Beeld en Geluid in Hilversum. Hiervoor was ik werkzaam bij de Vrije Universiteit. ";
		String testa1 = "888TUNEWat is het toch heerlijk om zo de oefeningen in de openlucht te doen.Probeer het ook maar eens, net als wij hier in het Nationale Park Hoge Veluwe.Stappen. Beetje warm worden.En we gaan over naar 'n zijstap-tik.Doe maar mee!Zij-tik. Zij-tik.Beweeg de armen gewoon ontspannen mee...van voor naar de heupen.En die tik die we nu opzij doen, kan ook midvoor.Midvoor.Handen even in de zij.Zijstap-tik weer.Zijstap-tik.Lekker ontspannen.En we gaan weer naar midvoor.En de armen die gaan zo opzij mee naar midvoor.Nog 4, 3, 2...Zijstap-tik.Nou, en dat kan natuurlijk ook, naar midachter.Dus midachter.En de armen neem je ook een beetje mee naar achteren.Lekkere oefening om het lichaam warm te maken.Zijstap-tik!Handen even in de zij.We gaan een beetje door laag.Laag. Laag.Nog 4, 3...Midvoor! Tik maar aan.Met de hak.Nog 4, 3...En weer opzij.Door laag. Door laag.Laag.Nog 4, 3...Midachter.Tik maar aan.Nog 4, 3...En door laag.Ben je al een beetje warm?Dan gaat het goed.Midvoor!Nog 4, 3, 2...En door laag!Laag. Laag. Laag.Nog 4, 3...En we gaan stappen, hoor.Lekker warm.Klaar voor de hartslag.De hartslagoefening.Dus het tempo gaat omhoog. Willy laat het zien stappend.Dus als het te zwaar is, doe dan stappend mee met Willy.Stap op de plaats.Toevallig is het eerste gedeelte ook stappend.Want wat we gaan doen is vier stappen naar voren en vier terug.1, 2, 3, 4. En terug.Stevig stappen.Dus grote stappen.Tenminste als je de ruimte hebt in de woonkamer. En terug.En flink de armen mee bewegen.En terug.Laatste twee keer.Laatste keer.We gaan door met een dribbel op de plaats.Dribbel. Doe de knieen naar voren.En we nemen gelijk de armen mee omhoog. Strek ze maar uit.Op.Nou, is dat te zwaar dan mag je het ook zonder armbewegingen doen.En anders stappend, he.Houd nog even vol.Laatste acht keer.8.6.We gaan terug naar het eerste deel. Vier stappen naar voren.Vier stappen terug. Nog een keer.En terug.Armen bewegen goed mee.Lekker intensief.Laatste.Dribbel op de plaats. Armen omhoog.En goed blijven ademhalen.Laatste 8.6.4. 3.Blijf even staan.Handen op de heupen.Dan doen we de knie omhoog om en om.Beetje een stoere houding.Maar de schouders zijn wel laag.Een klein beetje naar achteren. En de knieen goed optrekken.Bovenlichaam rechtop.Laatste vier keer.4, 3...Vanaf het begin vier stappen naar voren.Voor.Terug.Voor.Laatste twee keer.En terug.Laatste. Nog even dat intensieve deel.Op de plaats. Armen omhoog.En dan ronden we 'm af.Laatste 8.6 keer.4, 3...En we gaan stappen.We gaan een balansoefening doen voor de benen.Maar het is eigenlijk met het hele lichaam.We gebruiken de flesjes erbij...en eventueel, als je balansproblemen hebt, een stoel.Kom rechtop staan.Buikspieren aanspannen, rugspieren aanspannen, kin in.Dus je wervelkolom mooi stabiliseren.Licht buigen in de knieen.En dan ga je staan op EEN been.Dus het standbeen blijft licht gebogen.Het vrije been til je op...en je spreidt de armen voor de balans.En houd dit even vast.En als je een stoel erbij nodig hebt...dan mag je de punt van je tenen licht op de stoel zetten...dat je daar een klein beetje contact houdt voor de balans.Nog 4, 3, 2...En wissel. Andere voet.Eerst je balans zoeken, op EEN been.Standbeen licht buigen. Buikspieren aanspannen. Knie omhoog.En je heupen ook mooi stabiel houden.Flesjes opzij.Die mogen in de balans helpen, he.Dat zie je ook bij die koorddansers, die houden ook hun armen gespreid voor de balans.Een beetje wankelen is niet erg. Maar pak het weer op.Zoek een vast punt waar je naar kijkt om het te houden.3, 2, 1.En ontspan.Even een beetje los schudden.Want dit is echt een concentratieoefening.We gaan weer op EEN been staan.Armen weer spreiden.Dan contact zoeken.Dezelfde arm als het been gaat omhoog.En dan breng je hem heel rustig naar de tegenovergestelde heup.Dus hoog.En heup.Hoog.En heup.Nog EEN keer. Want het is best lastig dit.En heup.En wissel.Ik sta een beetje versteld van mijn eigen balans hoor.Hoog.En heup.Hoog.Maar mijn geheim is dat ik mijn buikspieren goed aanspan...en ook mijn rugspieren, en ik hou mijn kin in.Dus mijn hele zwaartepunt ligt prachtig boven mijn voet.Laatste keer.En ontspan.We gaan een oefening doen voor de zijkant van de schouders en de voorkant van de bovenarmen.Kom staan met de voeten op heupbreedte.Even de juiste houding aannemen.Van bovenaf gezien kin in, schouders laag...en een klein beetje naar achteren. Knieen zijn licht gebogen en de buikspieren aangespannen.Je draait de flessen met de doppen bijna naar buiten...dus iets naar voren.En dan breng je ze zijwaarts omhoog.En weer terug.Doen we ietsje sneller.Op. En neer.Op. En neer.Van de zijkant bekeken zijn je armen iets voor de schouders.Dus niet helemaal recht naar de zijkant, maar iets meer naar voren.Houd de schouders laag tijdens de beweging.Laatste 4 keer.3.2.Dit is de laatste.Ellebogen in je zij.En de doppen wijzen nu iets schuin naar achteren.Houd vast. Armen gestrekt.Schouders nog steeds laag.En dan gaan we de armen buigen. Op. En neer.Op. En neer.Dit is een vrij eenvoudige beweging.Dus je kunt ook meteen je aandacht vestigen op je houding.Knieen zijn licht gebogen, buikspieren aangespannen.Schouders laag, een beetje naar achteren. En de kin is in.Laatste 4 keer.3.2.Blijf even hier. We gaan combineren.De eerste beweging is zijwaarts heffen. Dan kom je terug.Draai je de doppen iets schuin naar achteren. Arm buigen.Doe maar mee. Op.Arm buigen. Omhoog.Op.En buig de armen.Loop voor jezelf ook nog even de punten door van je houding.Kin in, schouders laag, buikspieren aangespannen.En de knieen zijn licht gebogen.Voor de schouders. En de bovenarmen.Schouders.Bovenarmen.Als je de juiste houding aanneemt met de spanning op je buikspieren...dan train je die ook meteen een beetje mee. Da's altijd meegenomen.We gaan voor de laatste 4 keer.Da's 4.En op.En 3.En dan gaan we de laatste twee versnellen.1. En op.En de laatste.Heel goed.En we gaan stappen.Buiten zit heerlijk veel zuurstof en die gaan we ook inademen, he.Zet thuis ook maar de ramen open. Kom dan makkelijk staan.En dan met twee armen heerlijk om je lichaam.Maak een ruime beweging.Adem in, door de neus.En blaas uit als je naar achteren gaat.In.En blaas weer uit.Goed uitblazen. Adem in.Blaas weer uit.In.Blaas weer uit.Met EEN arm. Adem in.Blaas weer uit.Adem in.Blaas weer uit.Twee armen. Adem in.Blaas weer uit.Met EEN arm. Adem in.Blaas weer uit. EEN arm terug.In.Blaas weer uit. Met twee armen. Voorlangs.Blaas uit. En spreid de armen.Heerlijk.Maak je borstkas ruim.Adem in.Blaas weer uit.Lang uitblazen.Twee armen naar voren. Adem in.Blaas uit met een zigzag.Twee armen. Adem in.Blaas weer uit.Armen spreiden. Adem in.En blaas weer uit.Adem in.Gelijk je rug een beetje rondmaken van boven.En blaas weer uit.Strek je uit.Laatste keer. Adem in.En blaas weer uit.Twee armen naar EEN kant.Met een ruime zwaai.Helemaal naar de andere kant.Blaas uit.Adem in.En het zit er weer op hoor voor vandaag. Even nog wat uitstappen.Morgen zijn we er weer. Tot dan!Zaterdag is in Ureterp in Friesland weer de Nieuwjaarswandeltocht.U kunt kiezen voor 25 of 35 km.Eerst loopt u door 't door landelijk gebied richting Frieschepalen...daarna door het bos van Bakkeveen en de bossen van Wijnjewoude.De 35 km doorkruist ook nog de Duurswoudeheide...het grootst overgebleven heideveld van Friesland. Informatie: flal.nlEn zaterdag en zondag is in Schoorl in Noord-Holland...de Nieuwjaarswandeltocht de Schoorlse Duinen.U wandelt door de bossen en duinen tussen Schoorl, Bergen aan Zee en Camperduin.U kunt met eigen ogen zien hoeveel schade het gebied heeft opgelopen door brandstichting.De half verbrande bomen hebben plaatsgemaakt voor 'wandelende duinen'.U kunt uit diverse afstanden kiezen. Informatie: sportwandelschool.nlNPO ONDERTITELING TT888, 2014 tt888reacties(a)omroep.nl";
		try {
			ArrayList<NamedEntity> result =  gogo.getNamedEntitiesFromCLTL(testEx1);
			//ArrayList result = gogo.getNamedEntitiesFromCLTL(test1);
			System.out.println(result.toString());
		} catch (IOException | DocumentException e ) {
			e.printStackTrace();
				}
		
	}

}
