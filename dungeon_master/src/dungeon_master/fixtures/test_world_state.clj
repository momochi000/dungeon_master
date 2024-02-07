(ns dungeon-master.fixtures.test-world-state
  (:import [org.neo4j.driver GraphDatabase]
           [org.neo4j.driver AuthTokens]
           [org.neo4j.driver Values]
           [org.neo4j.driver TransactionWork])
  (:require [cheshire.core :as json]
            [dungeon-master.config :refer [database-url]]
            [dungeon-master.game-state :refer [->GameState]]
            [dungeon-master.game.data.character-sheet :refer [build-blank-char-sheet]]
            [dungeon-master.repositories.world-state
             :refer [create-node
                     create-relationship-from-string]]))

;; These are the statements we want to run.
;;-- CREATE CONSTRAINT IF NOT EXISTS FOR (p:Person) REQUIRE (p.name) IS UNIQUE;
;;CREATE INDEX IF NOT EXISTS FOR (p:Person) ON (p.name);
;;-- CREATE CONSTRAINT IF NOT EXISTS FOR (p:Place) REQUIRE (p.name) IS UNIQUE;
;;CREATE INDEX IF NOT EXISTS FOR (p:Place) ON (p.name);

;;CREATE (coran:Person {name:'Coran', description:'The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate'})
;;CREATE (lordDhelt:Person {name:'Lord Dhelt', description:'A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help'})
;;CREATE (blushingMermaidTavern:Place {name:'Blushing Mermaid Tavern', description:'The tavern in Baldur's Gate known for a warm atmosphere and busy clientele'})
;;CREATE (baldursGate:Place {name:'Baldur\'s Gate', description:'The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran'})
;;CREATE
;;  (coran)-[:IN]->(blushingMermaidTavern),
;;  (lordDhelt)-[:IN]->(blushingMermaidTavern),
;;  (blushingMermaidTavern)-[:IN]->(baldursGate)


(def fixture-json-string
"{
    \"entities\": [
        {\"label\":\"Person\",\"id\":\"coran\",\"name\":\"Coran\",\"description\":\"The stout bartender with a ruddy face who works at the Blushing Mermaid Tavern in Baldur's Gate\"},
        {\"label\":\"Person\",\"id\":\"lordDhelt\",\"name\":\"Lord Dhelt\",\"description\":\"A nobleman from Amn who is currently in the Blushing Mermaid Tavern and in need of discreet help\"},
        {\"label\":\"Place\",\"id\":\"blushingMermaidTavern\",\"name\":\"Blushing Mermaid Tavern\",\"description\":\"The tavern in Baldur's Gate known for a warm atmosphere and busy clientele\"},
        {\"label\":\"Place\",\"id\":\"baldursGate\",\"name\":\"Baldur's Gate\",\"description\":\"The city where the Blushing Mermaid Tavern is located and where business is always good according to Coran\"}
    ],
    \"relationships\": [
        \"coran|IN|blushingMermaidTavern\",
        \"lordDhelt|IN|blushingMermaidTavern\",
        \"blushingMermaidTavern|IN|baldursGate\"
    ]
}")

;;(require '[dungeon-master.game-state :refer [->GameState]])

(defn test-game-state
  []
  (->GameState
    :normal
    {}
    [
     {:role "assistant" :content "As you push open the heavy, wooden door of the Blushing Mermaid Tavern, a warm mixture of scents—roasted meats, spilt ale, and the smoky tang of a wood-burning hearth—greets you. The tavern is alive with the clamor of midday patrons: travelers swapping tales, locals enjoying their breaks, and off-duty guards seeking respite with a pint. \n\nThe tavern's interior is cozy and somewhat dimly lit, with sunlight streaming through small, high windows. A long bar runs along one wall, behind which a stout bartender with a ruddy face is vigorously polishing mugs. A scattering of wooden tables is spread throughout the common room, most occupied, though a few near the back stand empty. From a corner stage, a half-elf bard plucks a lute, adding a melodic backdrop to the hum of conversation."}
     {:role "assistant" :content "The bartender, a burly man with forearms like tree trunks, gives you a nod as he finishes up pouring a frothy ale for another patron. He then makes his way over to you, wiping his hands on a well-used cloth tucked into his apron. \"Welcome to the Blushing Mermaid,\" he says with a gruff but friendly voice. \"What will it be?\"\n\nHis eyes are attentive, clearly sizing you up as a newcomer – perhaps wondering if you're just passing through or if you've got a story to tell. Around you, the tavern continues its bustling activity, a few patrons glancing briefly in your direction before returning to their drinks and conversations."}
     {:role "assistant" :content "The bartender's face crinkles into a semblance of a smile as he takes the coins, giving the extra a little tap of appreciation before he turns to fetch your drink. \"Business is always good in Baldur's Gate,\" he replies, fetching a clean mug and filling it from a large barrel marked with a honeycomb pattern. \"Especially here at the Mermaid. Folk need a place to relax or drown their worries. More the latter, these dark days.\"\n\nHe slides the mead across to you—a golden liquid with a sweet aroma that promises to be refreshing. \"Anything else I can do for ya? A bite to eat, maybe? Or some news or rumors if you're the type seeking work or adventure.\" The bartender leans on the counter slightly, showing a genuine sense of interest—or perhaps just good business sense. It's clear he's someone who might know a great deal about what goes on in the city."}
     {:role "assistant" :content "The mead is smooth and satisfying, with just the right amount of sweetness to take the edge off a long day. Its subtle notes of honey and herbs linger pleasantly as you set your mug down, a good batch by any measure.\n\nThe bartender's expression grows serious as you mention your exploits. \"Goblins, eh? Nasty vermin,\" he grunts. Then, leaning in a bit closer and lowering his voice, he continues, \"As for work, if you're not picky, there's always guardsmen looking for an extra sword arm—what with the city's troubles. But if you're after something that might... let's say 'pay better,' I hear there's some folk who've got troubles that gold can solve. If you catch my meaning.\"\n\nHe glances around subtly, as though ensuring no one is eavesdropping. \"Saw a fella earlier, noble-like, having a quiet word with some rough types by the corner. Seemed to be in some sort of a bind. Might be he's still about if you move quickly. And there's always the mercenaries' contracts on the board yonder\"—he nods towards a corkboard on the far wall, cluttered with various notices and requests. \n\n\"Take your pick, and fill your purse. Just be careful, lad. Some troubles cut deeper than a goblin's blade.\" The bartender's advice carries genuine caution, suggesting that while the opportunities in Baldur's Gate are plentiful, they're not without risk."}
     {:role "assistant" :content "The bartender offers a slight nod, his eyes momentarily reflecting a bit of the wear and tear of a life spent within the thick walls of the tavern. \"Name's Coran,\" he says, extending a hand calloused from years of hard work behind the bar. \"Anything else you need, just holler.\"\n\nCoran then proceeds to assist another customer at the other end of the bar, giving you the space to either approach the noble or check out the notice board for any mercenary work. The atmosphere in the tavern hums with the potential for opportunity, whether it be quiet dealings with troubled nobles or the promise of adventure and coin pinned to the board."}
     {:role "assistant" :content "You carry your mug and casually move closer to where Coran indicated, all while keeping a relaxed demeanor. You find a spot where you can quietly observe the noble without drawing undue attention to yourself.\n\nThe nobleman is a well-dressed individual with an air of urgency about him, easily standing out from the rougher clientele of the tavern. While his clothes are of fine quality, there is a certain dishevelment to his appearance that suggests recent distress. You notice a signet ring with a family crest—an indicator of his status and wealth—on his finger as he nervously taps it on the table.\n\nHe's currently alone, nursing a drink of his own, and his eyes frequently dart toward the entrance, as if expecting someone. The lines of worry on his youthful face suggest that whatever burden he carries, it is both heavy and immediate.\n\nYou sense that timing might be crucial in approaching him. He looks as though he's on the verge of leaving or making a decision. Your warrior instincts tell you that offering your services could be both opportune and potentially rewarding, whether it be in the form of gold or alliances."}
     {:role "assistant" :content "The nobleman seems taken aback for a moment as you sit down, but your easy smile and non-threatening demeanor seem to put him partially at ease. He studies you for a moment, his eyes running over your warrior's build and the confidence with which you carry yourself. He then offers a weak smile in return, although the worry doesn't entirely leave his eyes.\n\n\"Indeed, it seems the day has decided to serve me a cold dish of troubles rather than a warm meal,\" he says, his voice carrying a cultured accent. He hesitates, as if weighing his need for help against the risk of trusting a stranger. \"I am Lord Dhelt of Amn, and I find myself in... a certain predicament that requires discretion and... a certain set of skills.\"\n\nHe takes a moment to ensure no one is eavesdropping before continuing. \"The 'gentlemen' you speak of were less than gentlemanly once their true colors shone through. I'm in need of someone who can handle... delicate matters with strength and resolve. Perhaps I am fortunate that the Sour Mother sent you to my table instead of further ill tidings.\"\n\nHis posture shifts slightly, indicating a readiness to discuss business. \"If you're willing to lend your blade and your wits to a noble cause, I can assure you that you will be handsomely rewarded,\" Lord Dhelt offers with a hopeful note in his voice, the promise of gold clear in his implication."}
     ]
    (build-blank-char-sheet "Torynn")))

(defn insert-test-world-state
  "insert some fixed data into the graph db"
  [world-data]
  ;;(with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
  (with-open [driver (GraphDatabase/driver database-url (AuthTokens/none))]
      (with-open [session (.session driver)]
        (let [entities (world-data "entities")
              relationships (world-data "relationships") ]

          (doall (map
                   (fn [entity-data]
                     (create-node entity-data session))
                   entities))

          (doall (map
                   (fn [relationship-data]
                     (create-relationship-from-string relationship-data session))
                   relationships))))))


;; TODO: use Values/parameters rather than just clojure maps to pass the args
  ;; couldn't get this to work. complains of 6 params being passed to Values.parameters
  ;; other means of calling `parameters` seemed to complain of things relating
  ;; to clojure data types not matching up with java datatypes



;; testing this out

;; Run these in repl or editor to have access to the imports needed in this namespace
;;(import '(org.neo4j.driver TransactionWork))
;;(import '(org.neo4j.driver GraphDatabase))
;;(import '(org.neo4j.driver AuthTokens))
;;(import '(org.neo4j.driver Values))
;;(require '[cheshire.core :as json])
;;(require '[dungeon-master.repositories.world-state :refer [create-node create-relationship-from-string]])
;;(require '[dungeon-master.game-state :refer [->GameState]])
;;(require '[dungeon-master.game.data.character-sheet :refer [build-blank-char-sheet]])

;;(insert-test-world-state (json/parse-string fixture-json-string))


;;(defn test-create-relationship
;;  "insert some fixed data into the graph db"
;;  []
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;      (with-open [session (.session driver)]
;;        (create-relationship-from-string  "coran|IN|blushingMermaidTavern" session))))

;; From within the compose cluster, use graphdb as the hostname of the neo4j instance
;; but running in my repl, i can access it as localhost
;;(defn test-session
;;  "simply providing a session to test individual queries, takes in the function requring the session"
;;  [query-func data]
;;  ;;(with-open [driver (GraphDatabase/driver "bolt://graphdb:7687" (AuthTokens/none))]
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (query-func data session))))


;;
;;
;;(def sample-person
;;  {:id "coran" :name "Coran" :description "some test description"})
;;
;;(print (sample-person :id))
;;
;;(def sample-person
;;  {"id" "coran" "name" "Coran" "description" "some test description"})
;;
;;(print (sample-person "id"))
;;
;;
;;(test-session create-person-node sample-person)


;; parse the fixture json into a clojure map
;; the true here says make the keys into symbols. we might not want this though.
;; (json/parse-string fixture-json-string true)


;;(def people
;;  '(
;;    {:name "john" :description "a person"}
;;    {:name "tony" :description "a person"}
;;    {:name "amy" :description "a person"}
;;    ))
;;
;;
;;(defn create-node
;;  [node-data driver-session]
;;  (.writeTransaction
;;    driver-session
;;    (reify TransactionWork (execute [this tx]
;;                             (let [result
;;                                   (.run tx
;;                                         "MERGE (p:Person {name: $name, description: $description}) RETURN (p)"
;;                                         node-data)]
;;                               (.single result)
;;                               )))))
;;
;;(defn test-insert-records
;;  "insert some fixed data into the graph db"
;;  []
;;  (with-open [driver (GraphDatabase/driver "bolt://localhost:7687" (AuthTokens/none))]
;;    (with-open [session (.session driver)]
;;      (create-node (first people) session))))
;;
;;      ;;(map
;;      ;;  (fn [entity-data] (create-node entity-data session))
;;      ;;  people))))
;;
;;(test-insert-records)
