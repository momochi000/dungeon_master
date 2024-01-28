(ns dungeon-master.game.data.character-sheet)

(defrecord CharacterSheet
  [character-name
   character-class
   level
   race
   alignment
   experience
   player-name

   stats
   skills
   status

   ;;saving-throws ;;calculated
   proficiencies
   inventory
   equipped

   spells

   personality
   appearance
   ])


(defrecord Status
  [;;armor-class
   initiative
   inspiration
   ;;proficiency-bonus
   speed
   hit-dice
   max-hitpoints
   hitpoints
   temporary-hitpoints
   death-save-successes
   death-save-failures ])

(defrecord Personality
  [personality-traits
   ideals
   bonds
   flaws])

(defrecord Stats
  [strength
   dexterity
   constitution
   intelligence
   wisdom
   charisma ])

;; These are calculated
;;(defrecord SavingThrows
;;  [strength
;;   dexterity
;;   constituion
;;   intelligence
;;   wisdom
;;   charisma ])


(defn build-default-stat-sheet
  []
  (->Stats 8 8 8 8 8 8))

(defn build-default-status
  []
  (->Status 0 0 1 1 1 6 0 0 0))

(defn build-blank-char-sheet
  [character-name]

  (->CharacterSheet
    character-name
    nil
    1
    :human
    :true-neutral
    0
    ""
    (build-default-stat-sheet)
    ""
    (build-default-status)
    [] ;;proficiencies
    [] ;;inventory
    [] ;;equipped
    [] ;;spells
    (->Personality "" "" "" "")
    "" ;; appearance
    )
  )


;; Testing
;;(def test-char-sheet (build-blank-char-sheet "Jerome Jones"))
;;(:stats test-char-sheet)
