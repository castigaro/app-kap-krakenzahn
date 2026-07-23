package com.appsonar.kapkrakenzahn.world

import com.appsonar.kapkrakenzahn.data.ActiveEvent
import com.appsonar.kapkrakenzahn.data.GameState
import com.appsonar.kapkrakenzahn.game.Variation

/**
 * Die komplette unveränderliche Spielwelt von Kap Krakenzahn: Orte,
 * Gegenstände, Figuren, Regeln und Rätsel. Veränderlich ist ausschließlich
 * der [GameState]; die pro Durchlauf gewürfelten Unterschiede stecken in
 * [Variation] (deterministisch aus dem Seed des Spielstands).
 */
object GameWorld {

    // ---- Zustands-Prädikate ------------------------------------------------

    fun variation(state: GameState): Variation = Variation(state.seed)

    fun dornenFrei(s: GameState): Boolean =
        s.has(FlagId.DORNEN_ZERSCHNITTEN) || s.has(FlagId.ZIEGE_WEGGELOCKT)

    /** Trägt die Heldin eine funktionierende Lichtquelle? */
    fun hasLight(s: GameState): Boolean =
        s.has(ItemId.BRENNENDE_LATERNE) || s.has(ItemId.QUALLENEIMER) || s.has(ItemId.FACKEL)

    fun sturmAktiv(s: GameState): Boolean =
        s.activeEvents.any { it.typ == ActiveEvent.EVENT_STURM && it.restSchritte > 0 }

    fun springflutAktiv(s: GameState): Boolean =
        s.activeEvents.any { it.typ == ActiveEvent.EVENT_SPRINGFLUT && it.restSchritte > 0 }

    fun fieloBei(s: GameState, ort: LocationId): Boolean =
        s.activeEvents.any { it.typ == ActiveEvent.EVENT_FIELO && it.ort == ort && it.restSchritte > 0 }

    fun hoehleBegehbar(s: GameState): Boolean =
        s.has(FlagId.HOEHLE_ZUGANG) && !springflutAktiv(s)

    fun hatGlitzerkram(s: GameState): Boolean =
        s.has(ItemId.MEDAILLON) || s.has(ItemId.SPIEGEL)

    // ---- Gegenstände -------------------------------------------------------

    val items: Map<ItemId, Item> = listOf(
        Item(ItemId.MEDAILLON, "Medaillon", "Ein angelaufenes Silbermedaillon. Innen: das Bild eines jungen Piraten mit rotem Bart."),
        Item(ItemId.SPIEGEL, "Handspiegel", "Ein kleiner Handspiegel mit Perlmuttrahmen. Glitzert hübsch."),
        Item(ItemId.SANDDORNPUNSCH, "Becher Sanddornpunsch", "Riecht nach Sanddorn, Rum und schlechten Entscheidungen."),
        Item(ItemId.FEUERPFEFFER, "Feuerpfeffer", "Eine knallrote Schote. Schon das Anschauen treibt dir Tränen in die Augen."),
        Item(ItemId.SPEZIALPUNSCH, "Spezial-Punsch", "Sanddornpunsch mit Feuerpfeffer — dampft bedrohlich. Rasmus' Leibgetränk."),
        Item(ItemId.MACHETE, "Machete", "Bos beste Klinge. Schneidet durch Dornen wie durch Butter."),
        Item(ItemId.STINKFISCH, "Stinkfisch", "Yolas ältester Fisch. Selbst die Möwen halten Abstand."),
        Item(ItemId.LATERNE, "Sturmlaterne", "Eine solide Laterne — leider ohne einen Tropfen Öl."),
        Item(ItemId.TRANOEL, "Tranöl", "Ein Krug zäher Fischtran. Brennt hell und riecht streng."),
        Item(ItemId.BRENNENDE_LATERNE, "Brennende Laterne", "Die Sturmlaterne, gefüllt mit Tranöl. Ihr warmes Licht vertreibt jede Finsternis."),
        Item(ItemId.EIMER, "Holzeimer", "Ein simpler Holzeimer. Erstaunlich, wie oft man so etwas braucht."),
        Item(ItemId.QUALLENEIMER, "Quallen-Eimer", "Ein Eimer Meerwasser voller Leuchtquallen. Schimmert türkis und wunderschön."),
        Item(ItemId.STOCK, "Kräftiger Ast", "Armlang und griffig. Ein Fackelschaft, wie er im Buche steht."),
        Item(ItemId.TEER, "Teerklumpen", "Klebrig, schwarz, brennbar. Der Hafen dankt für die Entnahme."),
        Item(ItemId.TEERFACKEL, "Teerfackel", "Ast plus Teer. Fehlt nur noch ein Funke."),
        Item(ItemId.FACKEL, "Brennende Fackel", "Knistert und leuchtet. Höhlen, zittert!"),
        Item(ItemId.FEUERSTEIN, "Feuerstein", "Schlägt zuverlässig Funken."),
        Item(ItemId.SEIL, "Tauwerk", "Zwanzig Ellen bestes Hanfseil."),
        Item(ItemId.ENTERHAKEN, "Enterhaken", "Dreizinkig und rostig, aber stabil."),
        Item(ItemId.WURFANKER, "Wurfanker", "Seil und Enterhaken, seemännisch vertäut. Damit erklimmt man alles — oder seilt sich ab."),
        Item(ItemId.GEZEITENAMULETT, "Gezeiten-Amulett", "Maras Amulett aus Treibglas. Es summt leise im Rhythmus der Wellen."),
        Item(ItemId.MUSCHELHORN, "Muschelhorn", "Eine große Tritonsmuschel. Wer hineinbläst, weckt das Meer."),
        Item(ItemId.DIETRICH, "Dietrich", "Bos 'Universalwerkzeug'. Für rein rechtschaffene Zwecke, versteht sich."),
        Item(ItemId.SCHLUESSEL, "Leuchtturmschlüssel", "Griets schwerer Messingschlüssel zum Leuchtturm."),
        Item(ItemId.ERSATZSCHLUESSEL, "Ersatzschlüssel", "Ansgars versteckter Zweitschlüssel, noch voller Erde."),
        Item(ItemId.LAPPEN, "Putzlappen", "Ein abgewetzter, aber sauberer Lappen."),
        Item(ItemId.FASS, "Punschfass", "Ein volles Fass aus Griets Keller. Schwerer, als es aussieht."),
        Item(ItemId.KRAKENAUGE, "Krakenauge", "Der sagenhafte Edelstein aus dem Schlund der See. Er pulsiert in tiefem Türkis."),
    ).associateBy { it.id }

    fun itemName(id: ItemId): String = items.getValue(id).name

    fun itemDescription(id: ItemId): String = items.getValue(id).beschreibung

    // ---- Rätsel ------------------------------------------------------------

    val puzzles: List<Puzzle> = listOf(
        Puzzle(
            PuzzleId.ROTBARTS_GEHEIMNIS, "Rotbarts Geheimnis",
            "Käpt'n Rasmus Rotbart kennt den Abstieg zur Krakenhöhle — aber er verrät ihn nur, wenn man ihn beeindruckt.",
            listOf(
                SolutionPath("duell", "Besiege Rasmus im Duell der spitzen Zungen.", FlagId.DUELL_GEWONNEN),
                SolutionPath("punsch", "Serviere ihm seinen Spezial-Punsch (Sanddornpunsch + Feuerpfeffer).", FlagId.SPEZIALPUNSCH_SERVIERT),
                SolutionPath("medaillon", "Bring ihm sein verlorenes Medaillon zurück.", FlagId.MEDAILLON_ZURUECKGEGEBEN),
            ),
        ),
        Puzzle(
            PuzzleId.DER_DORNENPFAD, "Der Dornenpfad",
            "Dornenranken und eine sture Ziege versperren den Dschungelpfad hinter dem Markt.",
            listOf(
                SolutionPath("machete", "Schneide die Dornen mit Bos Machete frei.", FlagId.DORNEN_ZERSCHNITTEN),
                SolutionPath("ziege", "Locke die Dornenziege mit Yolas Stinkfisch weg.", FlagId.ZIEGE_WEGGELOCKT),
                SolutionPath("seitenpfad", "Finde mit Maras Hinweis und einer Lichtquelle den Seitenpfad am Strand.", FlagId.SEITENPFAD_ENTDECKT),
            ),
        ),
        Puzzle(
            PuzzleId.DIE_FLUTHOEHLE, "Die Fluthöhle",
            "Der Höhlenschlund liegt bei Flut unter Wasser — trockenen Fußes kommt niemand hinein.",
            listOf(
                SolutionPath("boot", "Rudere mit Griets Boot direkt in die Höhle.", FlagId.BOOT_GENUTZT),
                SolutionPath("amulett", "Bändige die Flut mit Maras Gezeiten-Amulett.", FlagId.FLUT_GEBAENDIGT),
                SolutionPath("abseilen", "Seile dich mit dem Wurfanker von der Klippenkante ab.", FlagId.ABGESEILT),
            ),
        ),
        Puzzle(
            PuzzleId.LICHT_INS_DUNKEL, "Licht ins Dunkel",
            "In der Krakenhöhle ist es stockfinster — ohne Licht geht nichts.",
            listOf(
                SolutionPath("laterne", "Fülle die Sturmlaterne mit Tranöl.", FlagId.LICHT_LATERNE),
                SolutionPath("quallen", "Fange Leuchtquallen im Eimer.", FlagId.LICHT_QUALLE),
                SolutionPath("fackel", "Baue eine Fackel aus Ast, Teer und Feuerstein.", FlagId.LICHT_FACKEL),
            ),
        ),
        Puzzle(
            PuzzleId.DES_WAECHTERS_RAETSEL, "Des Wächters Rätsel",
            "Der Geist des Wächters gibt das Krakenauge nur an Würdige heraus.",
            listOf(
                SolutionPath("raetsel", "Beantworte die drei Fragen des Wächters mit dem Wissen der Dorfbewohner.", FlagId.WAECHTER_RAETSEL_GELOEST),
                SolutionPath("musik", "Besänftige den Kraken mit dem Muschelhorn.", FlagId.KRAKE_BESAENFTIGT),
                SolutionPath("opfer", "Opfere dem Kraken etwas Glitzerndes im Opferbecken.", FlagId.OPFER_GEBRACHT),
            ),
        ),
        Puzzle(
            PuzzleId.DAS_NEUE_FEUER, "Das neue Feuer",
            "Die Leuchtturmtür ist verriegelt — und oben wartet das kalte Leuchtfeuer.",
            listOf(
                SolutionPath("schluessel", "Öffne die Tür mit Griets Leuchtturmschlüssel.", FlagId.TUER_MIT_SCHLUESSEL),
                SolutionPath("dietrich", "Knacke das Schloss mit Bos Dietrich.", FlagId.TUER_MIT_DIETRICH),
                SolutionPath("ersatz", "Finde Ansgars Ersatzschlüssel mit Krächz' Hinweis.", FlagId.TUER_MIT_ERSATZSCHLUESSEL),
            ),
        ),
    )

    fun puzzle(id: PuzzleId): Puzzle = puzzles.first { it.id == id }

    fun isSolved(state: GameState, id: PuzzleId): Boolean =
        puzzle(id).paths.any { state.has(it.solvedFlag) }

    fun solvedPath(state: GameState, id: PuzzleId): SolutionPath? =
        puzzle(id).paths.firstOrNull { state.has(it.solvedFlag) }

    // ---- Figuren -----------------------------------------------------------

    val npcs: Map<NpcId, Npc> = listOf(
        Npc(
            id = NpcId.GRIET, name = "Wirtin Griet", ort = LocationId.TAVERNE,
            begruessung = "Na, angeschwemmt worden? Setz dich, bevor du umfällst.",
            voicePitch = 0.85f, voiceRate = 1.05f,
            charakterkarte = "Griet, Wirtin der Taverne 'Zum Ersoffenen Kraken'. Resolut, " +
                "herzlich-ruppig, hat für jeden einen Spruch und für niemanden Mitleid. " +
                "Sie verwahrt seit Jahren den alten Leuchtturmschlüssel, gibt ihn aber nur " +
                "Leuten, denen sie vertraut. Gerade wartet unten am Anleger der Fährmann " +
                "auf ein Fass Sanddornpunsch, das ihr niemand tragen will.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.SANDDORNPUNSCH.name,
                    "schenkt der Spielerin einen Becher Sanddornpunsch aus",
                    "Kann ich einen Sanddornpunsch haben?", "Einen Sanddornpunsch für die Gestrandete! Geht aufs Haus.",
                    condition = { !it.has(ItemId.SANDDORNPUNSCH) && !it.has(ItemId.SPEZIALPUNSCH) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.FASS.name,
                    "gibt der Spielerin das Punschfass, das zum Anleger im Hafen gebracht werden muss (Gefälligkeit)",
                    "Kann ich dir irgendwie helfen?",
                    "Wenn du dich nützlich machen willst: Das Fass da muss zum Anleger, der Fährmann wartet. Ich geb dir auch was Gutes dafür.",
                    condition = { !it.has(ItemId.FASS) && !it.has(FlagId.GRIET_GEFALLEN) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_FLAG, FlagId.GRIET_AUFTRAG.name,
                    "merkt sich, dass sie die Spielerin um die Fass-Lieferung gebeten hat",
                    "Was gibt es zu tun?", "Das Fass zum Anleger, mehr verlang ich ja gar nicht.",
                    condition = { !it.has(FlagId.GRIET_AUFTRAG) && !it.has(FlagId.GRIET_GEFALLEN) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.SCHLUESSEL.name,
                    "vertraut der Spielerin den alten Leuchtturmschlüssel an (nur weil sie das Fass geliefert hat)",
                    "Hättest du den Schlüssel zum Leuchtturm für mich?",
                    "Für dich? Nach der Fass-Sache... na schön. Hier, Ansgars alter Schlüssel. Mach was draus.",
                    condition = { it.has(FlagId.GRIET_GEFALLEN) && !it.has(ItemId.SCHLUESSEL) && !it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
            ),
        ),
        Npc(
            id = NpcId.RASMUS, name = "Käpt'n Rasmus Rotbart", ort = LocationId.TAVERNE,
            begruessung = "Hicks. Wer wagt es, den Schrecken der sieben Pfützen anzusprechen?",
            voicePitch = 0.7f, voiceRate = 0.9f,
            charakterkarte = "Käpt'n Rasmus Rotbart, versoffener Ex-Pirat und unbesiegter Meister " +
                "im 'Duell der spitzen Zungen'. Großspurig, wehmütig, im Kern gutmütig. Er kennt " +
                "als Einziger den Abstieg von den Klippen zur Krakenhöhle, verrät ihn aber nur " +
                "Leuten, die ihn beeindrucken: wer ihn im Wortduell schlägt, ihm seinen " +
                "Spezial-Punsch serviert oder sein verlorenes Medaillon zurückbringt. Fordert man " +
                "ihn heraus, nimmt er hocherfreut an.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_DUELL, NpcId.RASMUS.name,
                    "nimmt die Herausforderung zum Duell der spitzen Zungen an",
                    "Ich fordere dich zum Duell der spitzen Zungen!",
                    "HA! Endlich wieder ein Opfer! Also gut, Kleine: wir duellieren uns — mit Worten, versteht sich.",
                    condition = { !it.has(FlagId.DUELL_GEWONNEN) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_FLAG, FlagId.WEG_ZUR_HOEHLE_BEKANNT.name,
                    "verrät den geheimen Abstieg von den Klippen zur Krakenhöhle (nur wenn die Spielerin ihn bereits beeindruckt hat)",
                    "Verrätst du mir jetzt den Weg zur Höhle?",
                    "Du hast es dir verdient. Auf halber Höhe des Klippenpfads, hinter dem Möwenfelsen, beginnt der alte Wärterabstieg. Aber pass auf die Flut auf!",
                    condition = {
                        (it.has(FlagId.DUELL_GEWONNEN) || it.has(FlagId.SPEZIALPUNSCH_SERVIERT) || it.has(FlagId.MEDAILLON_ZURUECKGEGEBEN)) &&
                            !it.has(FlagId.WEG_ZUR_HOEHLE_BEKANNT)
                    },
                ),
            ),
        ),
        Npc(
            id = NpcId.YOLA, name = "Marktfrau Yola", ort = LocationId.MARKT,
            begruessung = "Frischer Fisch! Na ja. Fisch! Na ja... Kauf einfach, Deern.",
            voicePitch = 1.2f, voiceRate = 1.15f,
            charakterkarte = "Yola, Marktfrau mit Fischstand und die Klatschbase des Kaps. " +
                "Redselig, gutmütig, weiß über jeden Bescheid. Verschenkt auch mal was aus " +
                "ihrem Stand (Stinkfisch, Feuerpfeffer, Tranöl), wenn man nett fragt oder " +
                "ihr zuhört.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.STINKFISCH.name,
                    "gibt der Spielerin ihren ältesten Stinkfisch",
                    "Hast du einen Fisch übrig, den keiner mehr will?",
                    "Den hier will nicht mal die Katze. Nimm ihn bloß mit, samt Gestank!",
                    condition = { !it.has(ItemId.STINKFISCH) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.FEUERPFEFFER.name,
                    "gibt der Spielerin eine Schote Feuerpfeffer",
                    "Verkaufst du auch Gewürze?",
                    "Feuerpfeffer, schärfste Sorte. Für dich umsonst — aber beschwer dich nachher nicht.",
                    condition = { !it.has(ItemId.FEUERPFEFFER) && !it.has(ItemId.SPEZIALPUNSCH) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.TRANOEL.name,
                    "gibt der Spielerin einen Krug Tranöl",
                    "Hast du Öl? Lampenöl vielleicht?",
                    "Tranöl, bester Brennstoff am Kap. Der Krug geht auf mich.",
                    condition = { !it.has(ItemId.TRANOEL) && !it.has(ItemId.BRENNENDE_LATERNE) },
                ),
            ),
        ),
        Npc(
            id = NpcId.BO, name = "Trödelschmied Bo", ort = LocationId.MARKT,
            begruessung = "Vorsicht, frisch geschmiedet. Oder frisch gefunden. Je nachdem.",
            voicePitch = 0.8f, voiceRate = 0.95f,
            charakterkarte = "Bo, der Trödelschmied: halb Schmied, halb Ramschhändler. Wortkarg, " +
                "trocken, hilfsbereit gegen Gegenleistung. Sein Ladekran im Hafen ist mit " +
                "verheddertem Seil lahmgelegt — wer das entwirrt, bekommt Werkzeug aus seinem " +
                "Fundus (Machete, Dietrich und was sonst herumliegt).",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_FLAG, FlagId.BO_AUFTRAG.name,
                    "bittet die Spielerin, das verhedderte Kranseil im Hafen zu entwirren",
                    "Brauchst du Hilfe bei irgendwas?",
                    "Mein Kran im Hafen. Seil völlig verheddert. Entwirr das, dann reden wir über Werkzeug.",
                    condition = { !it.has(FlagId.KRAN_ENTWIRRT) && !it.has(FlagId.BO_AUFTRAG) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.MACHETE.name,
                    "gibt der Spielerin seine Machete (nur weil sie den Kran entwirrt hat)",
                    "Kann ich die Machete haben?",
                    "Kran läuft wieder — abgemacht ist abgemacht. Hier, meine beste Klinge.",
                    condition = { it.has(FlagId.KRAN_ENTWIRRT) && !it.has(ItemId.MACHETE) && !dornenFrei(it) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.DIETRICH.name,
                    "gibt der Spielerin seinen Dietrich (nur weil sie den Kran entwirrt hat)",
                    "Hast du was, um ein Schloss zu öffnen?",
                    "Offiziell habe ich das nie besessen. Inoffiziell: bitte sehr.",
                    condition = { it.has(FlagId.KRAN_ENTWIRRT) && !it.has(ItemId.DIETRICH) && !it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.FEUERSTEIN.name,
                    "gibt der Spielerin einen Feuerstein (nur wenn sie den Kran entwirrt hat)",
                    "Hast du einen Feuerstein?",
                    "Feuerstein? Liegt hier irgendwo... da. Für meine Kran-Retterin.",
                    condition = { variation(it).feuersteinBeiBo && it.has(FlagId.KRAN_ENTWIRRT) && !it.has(ItemId.FEUERSTEIN) && !it.has(ItemId.FACKEL) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.ENTERHAKEN.name,
                    "gibt der Spielerin einen Enterhaken (nur wenn sie den Kran entwirrt hat)",
                    "Hast du so etwas wie einen Enterhaken?",
                    "Enterhaken, dreizinkig, kaum benutzt. Wer den Kran rettet, kriegt den Haken.",
                    condition = { variation(it).enterhakenBeiBo && it.has(FlagId.KRAN_ENTWIRRT) && !it.has(ItemId.ENTERHAKEN) && !it.has(ItemId.WURFANKER) },
                ),
            ),
        ),
        Npc(
            id = NpcId.MARA, name = "Einsiedlerin Mara", ort = LocationId.STRAND,
            begruessung = "Die Flut hat dich gebracht. Mal sehen, ob die Ebbe dich wieder mitnimmt.",
            voicePitch = 1.05f, voiceRate = 0.85f,
            charakterkarte = "Mara, die Einsiedlerin vom Strand. Wettergegerbt, wortkarg, " +
                "spricht in Gezeiten-Metaphern. Sie kennt das Meer, die Quallen und die " +
                "alten Pfade. Ihr Gezeiten-Amulett hält die Flut zurück — sie tauscht es " +
                "nur gegen etwas Glitzerndes (ein Medaillon oder einen Spiegel), niemals " +
                "umsonst. Beim Tausch nimmt sie den Glitzerkram an sich.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_ITEMWEG, ItemId.MEDAILLON.name,
                    "nimmt beim Amulett-Tausch das Medaillon der Spielerin an sich",
                    "Ich biete dir dieses Medaillon.", "Hübsch, wie es das Licht fängt... Das nehme ich.",
                    condition = { it.has(ItemId.MEDAILLON) && !it.has(ItemId.GEZEITENAMULETT) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEMWEG, ItemId.SPIEGEL.name,
                    "nimmt beim Amulett-Tausch den Handspiegel der Spielerin an sich",
                    "Ich biete dir diesen Spiegel.", "Ein Stück gefangenes Meer. Einverstanden.",
                    condition = { it.has(ItemId.SPIEGEL) && !it.has(ItemId.GEZEITENAMULETT) },
                ),
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, ItemId.GEZEITENAMULETT.name,
                    "übergibt ihr Gezeiten-Amulett — NUR im Tausch gegen Glitzerkram (Medaillon oder Spiegel), zusammen mit dem passenden itemweg-Effekt",
                    "Tauschst du dein Amulett gegen etwas Glitzerndes?",
                    "Glitzer gegen Gezeiten — ein alter Handel. Das Amulett gehört dir. Halt es an den Höhlenschlund, wenn die Flut gurgelt.",
                    condition = { hatGlitzerkram(it) && !it.has(ItemId.GEZEITENAMULETT) && !it.has(FlagId.FLUT_GEBAENDIGT) },
                ),
            ),
        ),
        Npc(
            id = NpcId.KRAECHZ, name = "Papagei Krächz", ort = LocationId.HAFEN,
            begruessung = "KRAAAH! Landratte an Backbord! Krächz alles sehen, Krächz alles plappern!",
            voicePitch = 1.6f, voiceRate = 1.25f,
            charakterkarte = "Krächz, der uralte Hafenpapagei. Frech, laut, wiederholt Wörter " +
                "doppelt und streut 'KRAAAH!' ein. Er saß jahrelang auf Ansgars Schulter und " +
                "plappert dessen Geheimnisse nach — man muss nur geschickt fragen. Er redet " +
                "in kurzen, abgehackten Papageiensätzen.",
            allowedEffects = emptyList(),
        ),
        Npc(
            id = NpcId.ANSGAR, name = "Ansgar Sturmvogel", ort = LocationId.HOEHLE,
            begruessung = "Wer... wer bringt Licht in mein Gefängnis? Der Krake duldet keine Diebe — und ich bin sein Wächter geworden.",
            voicePitch = 0.75f, voiceRate = 0.8f,
            charakterkarte = "Ansgar Sturmvogel, der verschwundene Leuchtturmwärter — halb Geist, " +
                "halb Gefangener des Kraken, dem er sein Versprechen brach. Würdevoll, müde, " +
                "voller Schuldgefühle. Er bewacht das Krakenauge und darf es nur Würdigen " +
                "überlassen: wer sein Rätsel besteht, den Kraken mit Musik besänftigt oder ein " +
                "Opfer bringt. Bietet die Spielerin an, sich dem Rätsel zu stellen, nimmt er sie " +
                "feierlich beim Wort. Die Antworten kennt er — verraten darf er sie NIEMALS.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_RAETSEL, "raetsel",
                    "beginnt die Prüfung: die drei Fragen des Wächters",
                    "Ich stelle mich deinem Rätsel.",
                    "So sei es. Drei Fragen, drei Antworten — irre einmal, und du beginnst von vorn.",
                    condition = { !it.has(FlagId.KRAKENAUGE_ERHALTEN) },
                ),
            ),
        ),
        Npc(
            id = NpcId.FIELO, name = "Fielo der fahrende Händler", ort = null,
            begruessung = "Psst! Fielo hat Ware, Fielo hat Preise — und heute: Fielo hat Geschenke!",
            voicePitch = 1.15f, voiceRate = 1.2f,
            charakterkarte = "Fielo, fahrender Händler mit einem Bauchladen voller Kuriositäten. " +
                "Taucht auf, wo man ihn nicht erwartet, redet schnell und blumig wie ein " +
                "Jahrmarktschreier. Heute verschenkt er ein Werbegeschenk an die erste Kundin, " +
                "die ihm ein bisschen Gesellschaft leistet.",
            allowedEffects = listOf(
                AllowedEffect(
                    AllowedEffect.TYP_ITEM, "FIELO_WARE",
                    "überreicht sein heutiges Werbegeschenk",
                    "Was hast du denn heute im Angebot?",
                    "Nur heute, nur für dich: dieses Prachtstück — geschenkt! Erzähl allen, Fielo war's.",
                    condition = { s -> !s.has(variation(s).fieloStock) },
                ),
            ),
        ),
    ).associateBy { it.id }

    fun npc(id: NpcId): Npc = npcs.getValue(id)

    // ---- Orte --------------------------------------------------------------

    private val sturmText =
        "Ein Sturm peitscht über den Klippenpfad — da hinauf weht es dich glatt ins Meer. Warte, bis er abzieht."
    private val flutText =
        "Gurgelnde Flut steht im Höhlenschlund. So kommst du da nicht trocken (oder lebend) hinein."

    val locations: Map<LocationId, Location> = listOf(
        Location(
            LocationId.HAFEN, "Hafen von Kap Krakenzahn",
            exits = listOf(
                Exit(LocationId.TAVERNE, 0.05f, "Zur Taverne"),
                Exit(LocationId.STRAND, 0.68f, "Zum Strand"),
                Exit(LocationId.MARKT, 0.95f, "Zum Markt"),
            ),
            hotspots = listOf(
                Hotspot(
                    "hafen_kraechz", "Papagei Krächz",
                    "Ein zerzauster Papagei mit dem Blick eines alten Seebären.",
                    RectFN(0.30f, 0.30f, 0.40f, 0.45f), anchorX = 0.35f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.KRAECHZ,
                ),
                Hotspot(
                    "hafen_kran", "Alter Ladekran",
                    "Bos Ladekran. Das Seil ist zu einem beeindruckenden Knoten verheddert.",
                    RectFN(0.42f, 0.15f, 0.58f, 0.75f), anchorX = 0.50f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
                Hotspot(
                    "hafen_tauwerk", "Tauwerk",
                    "Ordentlich aufgeschossenes Hanfseil. Niemand vermisst eine Rolle.",
                    RectFN(0.60f, 0.72f, 0.70f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "hafen_teerfass", "Teerfass",
                    "Ein offenes Fass voll zähem, schwarzem Teer.",
                    RectFN(0.72f, 0.62f, 0.82f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "hafen_eimer", "Holzeimer",
                    "Ein herrenloser Holzeimer, kaum löchrig.",
                    RectFN(0.85f, 0.74f, 0.92f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "hafen_anleger", "Anleger",
                    "Am Anleger dümpelt die Fähre. Der Fährmann schaut erwartungsvoll — als warte er auf eine Lieferung.",
                    RectFN(0.05f, 0.75f, 0.28f, 0.95f), anchorX = 0.16f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
                Hotspot(
                    "hafen_boot", "Griets Ruderboot",
                    "Ein gepflegtes Ruderboot mit 'GRIET' am Bug. Damit käme man übers Wasser bis in die Krakenhöhle.",
                    RectFN(0.10f, 0.60f, 0.26f, 0.74f), anchorX = 0.18f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
            ),
            walkY = 0.95f, walkFarY = 0.80f,
        ),
        Location(
            LocationId.TAVERNE, "Taverne 'Zum Ersoffenen Kraken'",
            exits = listOf(
                Exit(LocationId.HAFEN, 0.95f, "Zum Hafen"),
            ),
            hotspots = listOf(
                Hotspot(
                    "taverne_griet", "Wirtin Griet",
                    "Griet wienert einen Krug und behält dabei den ganzen Schankraum im Blick.",
                    RectFN(0.18f, 0.30f, 0.32f, 0.80f), anchorX = 0.25f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE, VerbId.BENUTZE), npc = NpcId.GRIET,
                ),
                Hotspot(
                    "taverne_rasmus", "Käpt'n Rasmus Rotbart",
                    "Ein Berg von einem Mann döst über drei leeren Bechern. Der rote Bart ist Legende, der Rest auch.",
                    RectFN(0.55f, 0.35f, 0.72f, 0.85f), anchorX = 0.63f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE, VerbId.BENUTZE), npc = NpcId.RASMUS,
                ),
                Hotspot(
                    "taverne_punschfass", "Punschfass",
                    "Griets Punschfass. Der Zapfhahn tropft einladend.",
                    RectFN(0.05f, 0.55f, 0.15f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "taverne_laterne", "Sturmlaterne",
                    "An einem Haken hängt eine alte Sturmlaterne. Trocken wie Griets Humor — kein Tropfen Öl.",
                    RectFN(0.40f, 0.18f, 0.48f, 0.34f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "taverne_lappen", "Putzlappen",
                    "Ein Putzlappen, ordentlich über die Theke gelegt.",
                    RectFN(0.33f, 0.60f, 0.40f, 0.70f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "taverne_regal", "Regal voller Krimskrams",
                    "Muscheln, Flaschenschiffe, Staub in Museumsqualität.",
                    RectFN(0.78f, 0.20f, 0.95f, 0.60f), anchorX = 0.86f,
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
            ),
            walkY = 0.95f, walkFarY = 0.84f,
        ),
        Location(
            LocationId.MARKT, "Marktgasse",
            exits = listOf(
                Exit(LocationId.HAFEN, 0.05f, "Zum Hafen"),
                Exit(
                    LocationId.DSCHUNGEL, 0.95f, "Zum Dschungelpfad",
                    condition = ::dornenFrei,
                    lockedText = "Mannshohe Dornenranken versperren den Pfad — und mittendrin glotzt dich eine sture Ziege an.",
                ),
            ),
            hotspots = listOf(
                Hotspot(
                    "markt_yola", "Marktfrau Yola",
                    "Yola sortiert Fische nach einem System, das nur sie versteht.",
                    RectFN(0.15f, 0.32f, 0.30f, 0.82f), anchorX = 0.22f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.YOLA,
                ),
                Hotspot(
                    "markt_bo", "Trödelschmied Bo",
                    "Bo hämmert gemächlich auf etwas herum, das mal ein Anker war. Oder wird.",
                    RectFN(0.52f, 0.30f, 0.67f, 0.82f), anchorX = 0.59f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.BO,
                ),
                Hotspot(
                    "markt_fischstand", "Fischstand",
                    "Fangfrisch bis fragwürdig. Ganz hinten liegt ein Exemplar, das schon Geschichte geschrieben hat.",
                    RectFN(0.05f, 0.60f, 0.14f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "markt_gewuerzkorb", "Gewürzkorb",
                    "Ein Korb knallroter Feuerpfeffer-Schoten. Die Luft darüber flimmert.",
                    RectFN(0.32f, 0.68f, 0.42f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "markt_trankrug", "Trankrug",
                    "Ein Krug Tranöl an Yolas Stand. 'Brennt wie Heimweh', steht auf dem Schild.",
                    RectFN(0.44f, 0.70f, 0.50f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "markt_troedelkiste", "Bos Trödelkiste",
                    "Eine Kiste voller Dinge, die mal wichtig waren.",
                    RectFN(0.69f, 0.66f, 0.80f, 0.85f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "markt_dornen", "Dornenranken",
                    "Fingerdicke Dornen, dahinter der Dschungelpfad. Mittendrin: eine Ziege mit dem Charme eines Zollbeamten.",
                    RectFN(0.84f, 0.30f, 0.98f, 0.85f), anchorX = 0.88f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = { !dornenFrei(it) },
                ),
            ),
            walkY = 0.95f, walkFarY = 0.78f,
        ),
        Location(
            LocationId.STRAND, "Strand mit Maras Hütte",
            exits = listOf(
                Exit(LocationId.HAFEN, 0.05f, "Zum Hafen"),
                Exit(
                    LocationId.DSCHUNGEL, 0.60f, "Seitenpfad in den Dschungel",
                    visible = { it.has(FlagId.SEITENPFAD_ENTDECKT) },
                ),
                Exit(
                    LocationId.KLIPPEN, 0.95f, "Zum Klippenpfad",
                    condition = { !sturmAktiv(it) },
                    lockedText = sturmText,
                ),
            ),
            hotspots = listOf(
                Hotspot(
                    "strand_mara", "Einsiedlerin Mara",
                    "Mara sitzt vor ihrer Treibholzhütte und flickt ein Netz, das keines mehr ist.",
                    RectFN(0.12f, 0.35f, 0.26f, 0.82f), anchorX = 0.19f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.MARA,
                ),
                Hotspot(
                    "strand_treibgut", "Treibgut",
                    "Angespültes Holz, Netzreste, Flaschen ohne Post.",
                    RectFN(0.30f, 0.70f, 0.44f, 0.88f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "strand_tuempel", "Gezeitentümpel",
                    "Ein stiller Tümpel zwischen den Felsen. Winzige Quallen glimmen darin wie versunkene Sterne.",
                    RectFN(0.48f, 0.76f, 0.62f, 0.92f),
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
                Hotspot(
                    "strand_felsen", "Strandfelsen",
                    "Glattgeschliffene Felsen. Dazwischen blitzt allerlei Angeschwemmtes.",
                    RectFN(0.64f, 0.62f, 0.76f, 0.86f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "strand_wrack", "Gestrandetes Wrack",
                    "Die Rippen eines alten Kutters ragen aus dem Sand wie Walknochen.",
                    RectFN(0.78f, 0.40f, 0.95f, 0.80f), anchorX = 0.84f,
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "strand_felsspalt", "Felsspalt",
                    "Ein enger Spalt in der Steilwand. Dahinter: Dunkelheit, die nach Dschungel riecht.",
                    RectFN(0.55f, 0.30f, 0.63f, 0.60f), anchorX = 0.59f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = { !it.has(FlagId.SEITENPFAD_ENTDECKT) },
                ),
            ),
            walkY = 0.95f, walkFarY = 0.74f,
        ),
        Location(
            LocationId.DSCHUNGEL, "Dschungelpfad",
            exits = listOf(
                Exit(LocationId.MARKT, 0.05f, "Zur Marktgasse"),
                Exit(
                    LocationId.STRAND, 0.30f, "Seitenpfad zum Strand",
                    visible = { it.has(FlagId.SEITENPFAD_ENTDECKT) },
                ),
                Exit(
                    LocationId.HOEHLE, 0.95f, "In den Höhlenschlund",
                    condition = ::hoehleBegehbar,
                    lockedText = flutText,
                ),
            ),
            hotspots = listOf(
                Hotspot(
                    "dschungel_gebuesch", "Dorniges Gebüsch",
                    "Ein Gebüsch mit Anspruch. Irgendetwas hat sich tief darin verfangen.",
                    RectFN(0.20f, 0.55f, 0.36f, 0.85f), anchorX = 0.28f,
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "dschungel_ast", "Kräftiger Ast",
                    "Ein armlanger Ast, wie gemacht für eine Fackel — oder um wichtig auszusehen.",
                    RectFN(0.42f, 0.72f, 0.54f, 0.86f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                ),
                Hotspot(
                    "dschungel_schlund", "Höhlenschlund",
                    "Der Eingang zur Krakenhöhle. Bei Flut schwappt schwarzes Wasser bis zur Decke.",
                    RectFN(0.80f, 0.30f, 0.98f, 0.88f), anchorX = 0.84f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
            ),
            walkY = 0.95f, walkFarY = 0.72f,
        ),
        Location(
            LocationId.KLIPPEN, "Klippenpfad",
            exits = listOf(
                Exit(
                    LocationId.STRAND, 0.05f, "Zum Strand",
                ),
                Exit(
                    LocationId.HOEHLE, 0.55f, "Wärterabstieg zur Höhle",
                    visible = { it.has(FlagId.WEG_ZUR_HOEHLE_BEKANNT) || it.has(FlagId.ABGESEILT) },
                    condition = ::hoehleBegehbar,
                    lockedText = flutText,
                ),
                Exit(LocationId.LEUCHTTURM, 0.95f, "Zum Leuchtturm"),
            ),
            hotspots = listOf(
                Hotspot(
                    "klippen_kante", "Klippenkante",
                    "Senkrecht hinab gähnt die See. Tief unten, halb im Wasser: der Schlund der Krakenhöhle.",
                    RectFN(0.35f, 0.55f, 0.55f, 0.90f), anchorX = 0.45f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                ),
                Hotspot(
                    "klippen_moewenfels", "Möwenfelsen",
                    "Ein Felszacken voller empörter Möwen. Dahinter könnte ein Pfad beginnen — wenn man wüsste, wo.",
                    RectFN(0.58f, 0.35f, 0.72f, 0.80f), anchorX = 0.64f,
                    verbs = setOf(VerbId.SCHAU),
                ),
            ),
            walkY = 0.95f, walkFarY = 0.84f,
        ),
        Location(
            LocationId.HOEHLE, "Krakenhöhle",
            exits = listOf(
                Exit(LocationId.DSCHUNGEL, 0.05f, "Hinaus zum Dschungelpfad"),
                Exit(
                    LocationId.KLIPPEN, 0.95f, "Aufstieg zum Klippenpfad",
                    visible = { it.has(FlagId.WEG_ZUR_HOEHLE_BEKANNT) || it.has(FlagId.ABGESEILT) },
                ),
            ),
            hotspots = listOf(
                Hotspot(
                    "hoehle_dunkel", "Dunkelheit",
                    "Finsternis, dick wie Teer. Ohne Licht siehst du nicht einmal deine Hand.",
                    RectFN(0.30f, 0.20f, 0.70f, 0.80f), anchorX = 0.50f,
                    verbs = setOf(VerbId.SCHAU),
                    condition = { !hasLight(it) },
                ),
                Hotspot(
                    "hoehle_ansgar", "Ansgar Sturmvogel",
                    "Eine halb durchscheinende Gestalt in Wärtersuniform. Seine Augen leuchten wie ferne Positionslichter.",
                    RectFN(0.40f, 0.30f, 0.54f, 0.80f), anchorX = 0.47f,
                    verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.ANSGAR,
                    condition = ::hasLight,
                ),
                Hotspot(
                    "hoehle_waechterstein", "Wächterstein",
                    "Ein von Tentakeln umschlungener Monolith. In seiner Mitte pulsiert eine leere Fassung — wie geschaffen für einen Edelstein.",
                    RectFN(0.60f, 0.25f, 0.75f, 0.85f), anchorX = 0.67f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = ::hasLight,
                ),
                Hotspot(
                    "hoehle_opferbecken", "Opferbecken",
                    "Ein flaches Steinbecken voller Perlmutt und alter Gaben. Der Krake nimmt, was glitzert.",
                    RectFN(0.20f, 0.65f, 0.36f, 0.88f), anchorX = 0.28f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = ::hasLight,
                ),
                Hotspot(
                    "hoehle_wasser", "Schwarzes Wasser",
                    "Unter der Oberfläche gleitet etwas Großes vorbei. Sehr Großes. Es scheint zu lauschen.",
                    RectFN(0.78f, 0.60f, 0.97f, 0.92f), anchorX = 0.82f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = ::hasLight,
                ),
            ),
            walkY = 0.94f, walkFarY = 0.84f,
        ),
        Location(
            LocationId.LEUCHTTURM, "Leuchtturm von Kap Krakenzahn",
            exits = listOf(
                Exit(
                    LocationId.KLIPPEN, 0.05f, "Zum Klippenpfad",
                    condition = { !sturmAktiv(it) },
                    lockedText = sturmText,
                ),
            ),
            hotspots = listOf(
                Hotspot(
                    "turm_tuer", "Turmtür",
                    "Eine wuchtige Tür aus Wrackholz. Verriegelt, versalzen, verschlossen.",
                    RectFN(0.40f, 0.40f, 0.56f, 0.88f), anchorX = 0.48f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = { !it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
                Hotspot(
                    "turm_stein", "Loser Stein",
                    "Ein Haufen Steine am Turmfuß. Einer davon liegt auffällig locker.",
                    RectFN(0.60f, 0.74f, 0.70f, 0.88f),
                    verbs = setOf(VerbId.SCHAU, VerbId.NIMM),
                    condition = { !it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
                Hotspot(
                    "turm_fassung", "Fassung des Leuchtfeuers",
                    "Oben im Lampenraum: eine kunstvoll geschmiedete Fassung — leer. Hier gehört das Krakenauge hinein.",
                    RectFN(0.38f, 0.15f, 0.58f, 0.45f), anchorX = 0.48f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = { it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
                Hotspot(
                    "turm_spiegel", "Reflektorspiegel",
                    "Der große Reflektor hinter der Fassung — blind vor Salz und Staub.",
                    RectFN(0.62f, 0.15f, 0.80f, 0.50f), anchorX = 0.70f,
                    verbs = setOf(VerbId.SCHAU, VerbId.BENUTZE),
                    condition = { it.has(FlagId.LEUCHTTURM_OFFEN) },
                ),
            ),
            walkY = 0.95f, walkFarY = 0.86f,
        ),
    ).associateBy { it.id }

    fun location(id: LocationId): Location = locations.getValue(id)

    /** Sichtbare Hotspots eines Orts — inklusive Fielo, falls er hier steht. */
    fun hotspotsFor(state: GameState, id: LocationId): List<Hotspot> {
        val basis = location(id).hotspots.filter { it.condition(state) }
        if (!fieloBei(state, id)) return basis
        return basis + Hotspot(
            "fielo", "Fielo der fahrende Händler",
            "Fielo balanciert seinen Bauchladen wie ein ganzes Warenhaus.",
            RectFN(0.02f, 0.40f, 0.12f, 0.85f), anchorX = 0.09f,
            verbs = setOf(VerbId.SCHAU, VerbId.REDE), npc = NpcId.FIELO,
        )
    }

    /** Sichtbare Ausgänge eines Orts. */
    fun exitsFor(state: GameState, id: LocationId): List<Exit> =
        location(id).exits.filter { it.visible(state) }

    // ---- Regeln ------------------------------------------------------------

    val rules: List<Rule> = buildList {
        // -- Hafen
        add(Rule(
            LocationId.HAFEN, "hafen_kran", VerbId.BENUTZE,
            condition = { !it.has(FlagId.KRAN_ENTWIRRT) },
            text = "Du zerrst, fluchst, zerrst wieder — und plötzlich fällt der Knoten auseinander. Der Kran schwenkt wie neu. Bo wird Augen machen.",
            effects = listOf(Effect.SetFlag(FlagId.KRAN_ENTWIRRT), Effect.PlaySfx("erfolg")),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_kran", VerbId.SCHAU,
            condition = { it.has(FlagId.KRAN_ENTWIRRT) },
            text = "Der Kran läuft wieder wie geschmiert. Bo schuldet dir was.",
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_tauwerk", VerbId.NIMM,
            condition = { !it.has(ItemId.SEIL) && !it.has(ItemId.WURFANKER) },
            text = "Du schulterst eine Rolle bestes Hanfseil. Man weiß nie.",
            effects = listOf(Effect.AddItem(ItemId.SEIL), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_teerfass", VerbId.NIMM,
            condition = { !it.has(ItemId.TEER) && !it.has(ItemId.TEERFACKEL) && !it.has(ItemId.FACKEL) },
            text = "Du klaubst einen ordentlichen Klumpen Teer heraus. Klebrig, aber nützlich.",
            effects = listOf(Effect.AddItem(ItemId.TEER), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_eimer", VerbId.NIMM,
            condition = { !it.has(ItemId.EIMER) && !it.has(ItemId.QUALLENEIMER) },
            text = "Ein Eimer. Deiner jetzt.",
            effects = listOf(Effect.AddItem(ItemId.EIMER), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_anleger", VerbId.BENUTZE, usedItem = ItemId.FASS,
            text = "Du rollst das Fass über die Planken. Der Fährmann strahlt, verstaut es und pfeift ein Danklied. Griet wird sich freuen.",
            effects = listOf(
                Effect.RemoveItem(ItemId.FASS),
                Effect.SetFlag(FlagId.GRIET_GEFALLEN),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_boot", VerbId.BENUTZE,
            condition = { it.has(FlagId.GRIET_GEFALLEN) && !springflutAktiv(it) },
            text = "Mit Griets Segen legst du ab und ruderst die Küste entlang — direkt in den dunklen Schlund der Krakenhöhle.",
            effects = listOf(
                Effect.SetFlag(FlagId.BOOT_GENUTZT),
                Effect.SetFlag(FlagId.HOEHLE_ZUGANG),
                Effect.MoveTo(LocationId.HOEHLE),
                Effect.PlaySfx("wasser"),
            ),
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_boot", VerbId.BENUTZE,
            condition = { it.has(FlagId.GRIET_GEFALLEN) && springflutAktiv(it) },
            text = "Die Springflut schleudert Gischt über die Mole. Bei DEM Wellengang steigst du in kein Ruderboot.",
        ))
        add(Rule(
            LocationId.HAFEN, "hafen_boot", VerbId.BENUTZE,
            text = "Da steht in krakeliger Schrift 'GRIET' dran. Ohne ihre Erlaubnis rührst du das Boot besser nicht an.",
        ))

        // -- Taverne
        add(Rule(
            LocationId.TAVERNE, "taverne_punschfass", VerbId.NIMM,
            condition = { !it.has(ItemId.SANDDORNPUNSCH) && !it.has(ItemId.SPEZIALPUNSCH) },
            text = "Du zapfst einen Becher Sanddornpunsch. Griet hebt eine Braue, sagt aber nichts.",
            effects = listOf(Effect.AddItem(ItemId.SANDDORNPUNSCH), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_laterne", VerbId.NIMM,
            condition = { !it.has(ItemId.LATERNE) && !it.has(ItemId.BRENNENDE_LATERNE) },
            text = "Du nimmst die Sturmlaterne vom Haken. Ohne Öl ist sie nur hübsches Blech.",
            effects = listOf(Effect.AddItem(ItemId.LATERNE), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_lappen", VerbId.NIMM,
            condition = { !it.has(ItemId.LAPPEN) },
            text = "Ein Lappen. Das Fundament jeder großen Heldentat.",
            effects = listOf(Effect.AddItem(ItemId.LAPPEN), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_regal", VerbId.NIMM,
            condition = { variation(it).spiegelInTaverne && !it.has(ItemId.SPIEGEL) },
            text = "Zwischen Staub und Muschelschalen: ein Handspiegel mit Perlmuttrahmen. Den vermisst hier niemand.",
            effects = listOf(Effect.AddItem(ItemId.SPIEGEL), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_regal", VerbId.NIMM,
            text = "Nur Staub, leere Flaschen und ein Flaschenschiff mit Schlagseite.",
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_rasmus", VerbId.BENUTZE, usedItem = ItemId.SPEZIALPUNSCH,
            condition = { !it.has(FlagId.SPEZIALPUNSCH_SERVIERT) },
            text = "Rasmus schnuppert — und seine Augen werden groß. 'Spezial-Punsch! MEIN Spezial-Punsch!' Er leert den Becher in einem Zug und schlägt dir donnernd auf die Schulter: 'Frag mich, was du willst, Kleine. Der Weg zur Höhle? Hinter dem Möwenfelsen am Klippenpfad beginnt der alte Wärterabstieg!'",
            effects = listOf(
                Effect.RemoveItem(ItemId.SPEZIALPUNSCH),
                Effect.SetFlag(FlagId.SPEZIALPUNSCH_SERVIERT),
                Effect.SetFlag(FlagId.WEG_ZUR_HOEHLE_BEKANNT),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_rasmus", VerbId.BENUTZE, usedItem = ItemId.SANDDORNPUNSCH,
            text = "Rasmus nippt und verzieht das Gesicht. 'Wo ist der WUMMS? Das trinkt ja meine Großmutter zum Frühstück.' Da fehlt wohl noch eine gewisse Schärfe.",
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_rasmus", VerbId.BENUTZE, usedItem = ItemId.MEDAILLON,
            condition = { !it.has(FlagId.MEDAILLON_ZURUECKGEGEBEN) },
            text = "Rasmus starrt das Medaillon an, und der alte Seebär bekommt feuchte Augen. 'Mein Medaillon... das Bild meiner Mutter.' Er räuspert sich donnernd. 'Du hast was gut bei mir. Der Weg zur Höhle: hinter dem Möwenfelsen am Klippenpfad, der alte Wärterabstieg.'",
            effects = listOf(
                Effect.RemoveItem(ItemId.MEDAILLON),
                Effect.SetFlag(FlagId.MEDAILLON_ZURUECKGEGEBEN),
                Effect.SetFlag(FlagId.WEG_ZUR_HOEHLE_BEKANNT),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.TAVERNE, "taverne_griet", VerbId.BENUTZE, usedItem = ItemId.FASS,
            text = "Griet schnaubt: 'Nicht MIR bringen — dem Fährmann am Anleger!'",
        ))

        // -- Markt
        add(Rule(
            LocationId.MARKT, "markt_fischstand", VerbId.NIMM,
            condition = { !it.has(ItemId.STINKFISCH) },
            text = "Du angelst den legendären Stinkfisch heraus. Yola ruft: 'Endlich nimmt den mal wer!'",
            effects = listOf(Effect.AddItem(ItemId.STINKFISCH), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_gewuerzkorb", VerbId.NIMM,
            condition = { !it.has(ItemId.FEUERPFEFFER) && !it.has(ItemId.SPEZIALPUNSCH) },
            text = "Du nimmst vorsichtig eine Feuerpfeffer-Schote. Deine Fingerspitzen kribbeln schon.",
            effects = listOf(Effect.AddItem(ItemId.FEUERPFEFFER), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_trankrug", VerbId.NIMM,
            condition = { !it.has(ItemId.TRANOEL) && !it.has(ItemId.BRENNENDE_LATERNE) },
            text = "Ein Krug Tranöl. Riecht streng, brennt hell.",
            effects = listOf(Effect.AddItem(ItemId.TRANOEL), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_troedelkiste", VerbId.NIMM,
            condition = { !variation(it).spiegelInTaverne && !it.has(ItemId.SPIEGEL) },
            text = "Zuoberst in der Kiste: ein Handspiegel mit Perlmuttrahmen. Bo zuckt mit den Schultern — 'Lag nur rum.'",
            effects = listOf(Effect.AddItem(ItemId.SPIEGEL), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_troedelkiste", VerbId.NIMM,
            condition = { variation(it).muschelhornAmMarkt && !it.has(ItemId.MUSCHELHORN) },
            text = "Unter altem Blech: eine prächtige Tritonsmuschel. 'Nimm sie', brummt Bo, 'die tutet mir zu viel.'",
            effects = listOf(Effect.AddItem(ItemId.MUSCHELHORN), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_troedelkiste", VerbId.NIMM,
            text = "Rostige Nägel, ein einzelner Stiefel, drei Türklinken. Faszinierend, aber nutzlos.",
        ))
        add(Rule(
            LocationId.MARKT, "markt_dornen", VerbId.BENUTZE, usedItem = ItemId.MACHETE,
            text = "Die Machete singt durch die Ranken. Die Ziege beobachtet das Massaker, meckert beleidigt und trollt sich. Der Pfad ist frei!",
            effects = listOf(Effect.SetFlag(FlagId.DORNEN_ZERSCHNITTEN), Effect.PlaySfx("erfolg")),
        ))
        add(Rule(
            LocationId.MARKT, "markt_dornen", VerbId.BENUTZE, usedItem = ItemId.STINKFISCH,
            text = "Du wedelst mit dem Stinkfisch. Die Ziege hebt den Kopf, schnuppert verzückt — und trottet dem Gestank hinterher, quer durch die Dornen. Die Lücke, die sie reißt, reicht dir locker.",
            effects = listOf(
                Effect.RemoveItem(ItemId.STINKFISCH),
                Effect.SetFlag(FlagId.ZIEGE_WEGGELOCKT),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.MARKT, "markt_dornen", VerbId.BENUTZE,
            text = "Du zerrst an den Ranken und erntest zerkratzte Hände und einen vernichtenden Ziegenblick.",
        ))

        // -- Strand
        add(Rule(
            LocationId.STRAND, "strand_treibgut", VerbId.NIMM,
            condition = { variation(it).medaillonAmStrand && !it.has(ItemId.MEDAILLON) && !it.has(FlagId.MEDAILLON_ZURUECKGEGEBEN) && !it.has(FlagId.OPFER_GEBRACHT) },
            text = "Zwischen Tang und Planken blitzt Silber: ein Medaillon! Innen das Bild eines jungen Piraten mit rotem Bart.",
            effects = listOf(Effect.AddItem(ItemId.MEDAILLON), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.STRAND, "strand_treibgut", VerbId.NIMM,
            text = "Nasses Holz, alte Netze, eine Flasche ohne Botschaft. Nichts Brauchbares.",
        ))
        add(Rule(
            LocationId.STRAND, "strand_felsen", VerbId.NIMM,
            condition = { !variation(it).feuersteinBeiBo && !it.has(ItemId.FEUERSTEIN) && !it.has(ItemId.FACKEL) },
            text = "Zwischen den Felsen liegt ein faustgroßer Feuerstein. Funken garantiert.",
            effects = listOf(Effect.AddItem(ItemId.FEUERSTEIN), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.STRAND, "strand_felsen", VerbId.NIMM,
            text = "Steine. Sehr schöne Steine. Aber eben: Steine.",
        ))
        add(Rule(
            LocationId.STRAND, "strand_wrack", VerbId.NIMM,
            condition = { !variation(it).enterhakenBeiBo && !it.has(ItemId.ENTERHAKEN) && !it.has(ItemId.WURFANKER) },
            text = "Im Wrack verkeilt: ein rostiger, aber grundsolider Enterhaken. Den nimmst du mit.",
            effects = listOf(Effect.AddItem(ItemId.ENTERHAKEN), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.STRAND, "strand_wrack", VerbId.NIMM,
            condition = { !variation(it).muschelhornAmMarkt && !it.has(ItemId.MUSCHELHORN) },
            text = "Halb im Sand unter dem Wrack: eine prächtige Tritonsmuschel. Wenn man hineinbläst, muss das klingen wie das Meer persönlich.",
            effects = listOf(Effect.AddItem(ItemId.MUSCHELHORN), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.STRAND, "strand_wrack", VerbId.NIMM,
            text = "Das Wrack gibt heute nichts mehr her — nur Splitter und Geschichten.",
        ))
        add(Rule(
            LocationId.STRAND, "strand_tuempel", VerbId.BENUTZE, usedItem = ItemId.EIMER,
            text = "Du schöpfst behutsam Wasser samt Leuchtquallen. Der Eimer glimmt auf wie ein gefangener Sternenhimmel.",
            effects = listOf(
                Effect.RemoveItem(ItemId.EIMER),
                Effect.AddItem(ItemId.QUALLENEIMER),
                Effect.SetFlag(FlagId.LICHT_QUALLE),
                Effect.PlaySfx("wasser"),
            ),
        ))
        add(Rule(
            LocationId.STRAND, "strand_tuempel", VerbId.BENUTZE,
            text = "Mit bloßen Händen? Die Quallen kichern förmlich. Ein Gefäß müsste her.",
        ))
        add(Rule(
            LocationId.STRAND, "strand_felsspalt", VerbId.SCHAU,
            condition = { it.knows(HintId.HINT_SEITENPFAD) && hasLight(it) },
            text = "Mit deiner Lichtquelle erkennst du, was Mara meinte: hinter dem Spalt windet sich ein schmaler Pfad bergauf — direkt in den Dschungel! Du merkst dir den Weg.",
            effects = listOf(Effect.SetFlag(FlagId.SEITENPFAD_ENTDECKT), Effect.PlaySfx("erfolg")),
        ))
        add(Rule(
            LocationId.STRAND, "strand_felsspalt", VerbId.SCHAU,
            condition = { it.knows(HintId.HINT_SEITENPFAD) },
            text = "Das muss Maras Spalt sein — aber dahinter ist es stockdunkel. Ohne Licht wagst du dich da nicht hinein.",
        ))
        add(Rule(
            LocationId.STRAND, "strand_felsspalt", VerbId.SCHAU,
            text = "Ein enger Spalt im Fels. Dahinter? Dunkelheit. Könnte eine Höhle sein, könnte eine Sackgasse sein.",
        ))

        // -- Dschungel
        add(Rule(
            LocationId.DSCHUNGEL, "dschungel_gebuesch", VerbId.NIMM,
            condition = { !variation(it).medaillonAmStrand && !it.has(ItemId.MEDAILLON) && !it.has(FlagId.MEDAILLON_ZURUECKGEGEBEN) && !it.has(FlagId.OPFER_GEBRACHT) },
            text = "Autsch — aber es lohnt sich: tief im Gebüsch hängt ein silbernes Medaillon an einem Dorn. Innen das Bild eines jungen Piraten mit rotem Bart.",
            effects = listOf(Effect.AddItem(ItemId.MEDAILLON), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.DSCHUNGEL, "dschungel_gebuesch", VerbId.NIMM,
            text = "Du erntest nur Kratzer und ein empörtes Rascheln.",
        ))
        add(Rule(
            LocationId.DSCHUNGEL, "dschungel_ast", VerbId.NIMM,
            condition = { !it.has(ItemId.STOCK) && !it.has(ItemId.TEERFACKEL) && !it.has(ItemId.FACKEL) },
            text = "Ein kräftiger Ast. Fackelschaft-Qualität, würde Bo sagen.",
            effects = listOf(Effect.AddItem(ItemId.STOCK), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.DSCHUNGEL, "dschungel_schlund", VerbId.BENUTZE, usedItem = ItemId.GEZEITENAMULETT,
            condition = { !it.has(FlagId.FLUT_GEBAENDIGT) },
            text = "Du hältst das Amulett gegen den Schlund. Es summt tief — und das Wasser weicht gurgelnd zurück, als hielte das Meer den Atem an. Der Weg ist frei!",
            effects = listOf(
                Effect.SetFlag(FlagId.FLUT_GEBAENDIGT),
                Effect.SetFlag(FlagId.HOEHLE_ZUGANG),
                Effect.PlaySfx("wasser"),
            ),
        ))
        add(Rule(
            LocationId.DSCHUNGEL, "dschungel_schlund", VerbId.SCHAU,
            condition = ::hoehleBegehbar,
            text = "Der Schlund liegt frei. Aus der Tiefe weht kalte, salzige Luft.",
        ))

        // -- Klippen
        add(Rule(
            LocationId.KLIPPEN, "klippen_kante", VerbId.BENUTZE, usedItem = ItemId.WURFANKER,
            condition = { !it.has(FlagId.ABGESEILT) },
            text = "Der Anker verhakt sich mit sattem 'KLONK' im Fels. Hand über Hand seilst du dich die Klippe hinab — vorbei an empörten Möwen — bis in den Schlund der Krakenhöhle.",
            effects = listOf(
                Effect.SetFlag(FlagId.ABGESEILT),
                Effect.SetFlag(FlagId.HOEHLE_ZUGANG),
                Effect.MoveTo(LocationId.HOEHLE),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.KLIPPEN, "klippen_kante", VerbId.BENUTZE, usedItem = ItemId.SEIL,
            text = "Nur das Seil? Ohne Haken hält da oben nichts. Du bräuchtest etwas zum Verhaken.",
        ))
        add(Rule(
            LocationId.KLIPPEN, "klippen_moewenfels", VerbId.SCHAU,
            condition = { it.has(FlagId.WEG_ZUR_HOEHLE_BEKANNT) },
            text = "Genau wie Rasmus sagte: hinter dem Möwenfelsen beginnt der alte Wärterabstieg — schmale Stufen, in den Fels gehauen.",
        ))

        // -- Höhle
        add(Rule(
            LocationId.HOEHLE, "hoehle_waechterstein", VerbId.BENUTZE,
            condition = { !it.has(FlagId.KRAKENAUGE_ERHALTEN) },
            text = "Du legst die Hand auf den Monolithen. Ansgars Stimme hallt: 'Die Prüfung beginnt.'",
            effects = listOf(Effect.StartRiddle),
        ))
        add(Rule(
            LocationId.HOEHLE, "hoehle_wasser", VerbId.BENUTZE, usedItem = ItemId.MUSCHELHORN,
            condition = { !it.has(FlagId.KRAKENAUGE_ERHALTEN) },
            text = "Du setzt das Muschelhorn an und bläst. Ein Ton wie Walgesang rollt durch die Höhle. Das schwarze Wasser kräuselt sich — ein gewaltiger Tentakel erhebt sich, wiegt sich im Takt... und legt dir behutsam das Krakenauge vor die Füße.",
            effects = listOf(
                Effect.SetFlag(FlagId.KRAKE_BESAENFTIGT),
                Effect.SetFlag(FlagId.KRAKENAUGE_ERHALTEN),
                Effect.AddItem(ItemId.KRAKENAUGE),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.HOEHLE, "hoehle_opferbecken", VerbId.BENUTZE, usedItem = ItemId.MEDAILLON,
            condition = { !it.has(FlagId.KRAKENAUGE_ERHALTEN) },
            text = "Du legst das Medaillon ins Becken. Das Wasser schäumt kurz auf — als es sich glättet, ist das Medaillon fort, und in der Fassung des Wächtersteins pulsiert das Krakenauge. Ein Tausch, den das Meer angenommen hat.",
            effects = listOf(
                Effect.RemoveItem(ItemId.MEDAILLON),
                Effect.SetFlag(FlagId.OPFER_GEBRACHT),
                Effect.SetFlag(FlagId.KRAKENAUGE_ERHALTEN),
                Effect.AddItem(ItemId.KRAKENAUGE),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.HOEHLE, "hoehle_opferbecken", VerbId.BENUTZE, usedItem = ItemId.SPIEGEL,
            condition = { !it.has(FlagId.KRAKENAUGE_ERHALTEN) },
            text = "Du legst den Spiegel ins Becken. Das Wasser schäumt kurz auf — als es sich glättet, ist der Spiegel fort, und in der Fassung des Wächtersteins pulsiert das Krakenauge. Der Krake liebt Glitzerndes.",
            effects = listOf(
                Effect.RemoveItem(ItemId.SPIEGEL),
                Effect.SetFlag(FlagId.OPFER_GEBRACHT),
                Effect.SetFlag(FlagId.KRAKENAUGE_ERHALTEN),
                Effect.AddItem(ItemId.KRAKENAUGE),
                Effect.PlaySfx("erfolg"),
            ),
        ))
        add(Rule(
            LocationId.HOEHLE, "hoehle_opferbecken", VerbId.BENUTZE,
            text = "Der Krake nimmt nur, was glitzert. Dein Charme zählt leider nicht.",
        ))

        // -- Leuchtturm
        add(Rule(
            LocationId.LEUCHTTURM, "turm_tuer", VerbId.BENUTZE, usedItem = ItemId.SCHLUESSEL,
            text = "Der Messingschlüssel dreht sich schwer, dann springt das Schloss auf. Die Tür schwingt knarrend nach innen — die Wendeltreppe wartet.",
            effects = listOf(
                Effect.RemoveItem(ItemId.SCHLUESSEL),
                Effect.SetFlag(FlagId.TUER_MIT_SCHLUESSEL),
                Effect.SetFlag(FlagId.LEUCHTTURM_OFFEN),
                Effect.PlaySfx("tuer"),
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_tuer", VerbId.BENUTZE, usedItem = ItemId.DIETRICH,
            text = "Du stocherst, lauschst, fluchst leise — klick. Bo wäre stolz (und würde es abstreiten). Die Tür ist offen.",
            effects = listOf(
                Effect.RemoveItem(ItemId.DIETRICH),
                Effect.SetFlag(FlagId.TUER_MIT_DIETRICH),
                Effect.SetFlag(FlagId.LEUCHTTURM_OFFEN),
                Effect.PlaySfx("tuer"),
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_tuer", VerbId.BENUTZE, usedItem = ItemId.ERSATZSCHLUESSEL,
            text = "Ansgars Ersatzschlüssel passt wie angegossen. Die Tür schwingt auf.",
            effects = listOf(
                Effect.RemoveItem(ItemId.ERSATZSCHLUESSEL),
                Effect.SetFlag(FlagId.TUER_MIT_ERSATZSCHLUESSEL),
                Effect.SetFlag(FlagId.LEUCHTTURM_OFFEN),
                Effect.PlaySfx("tuer"),
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_tuer", VerbId.BENUTZE,
            text = "Verrammelt und verriegelt. Mit bloßen Händen bewegst du hier gar nichts.",
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_stein", VerbId.NIMM,
            condition = { it.knows(HintId.HINT_ERSATZSCHLUESSEL) && !it.has(ItemId.ERSATZSCHLUESSEL) && !it.has(FlagId.LEUCHTTURM_OFFEN) },
            text = "Du wuchtest den losen Stein beiseite — und da liegt er, noch voller Erde: Ansgars Ersatzschlüssel! Krächz hat nicht gelogen.",
            effects = listOf(Effect.AddItem(ItemId.ERSATZSCHLUESSEL), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_stein", VerbId.NIMM,
            text = "Steine wälzen, ohne zu wissen wonach? Deine Fingernägel protestieren.",
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_fassung", VerbId.BENUTZE, usedItem = ItemId.KRAKENAUGE,
            condition = { it.has(FlagId.SPIEGEL_POLIERT) },
            text = "Du setzt das Krakenauge in die Fassung. Es pulsiert — einmal, zweimal — dann flammt es auf! Der blanke Reflektor wirft einen mächtigen Lichtkegel über die Bucht. Das Leuchtfeuer von Kap Krakenzahn BRENNT!",
            effects = listOf(
                Effect.RemoveItem(ItemId.KRAKENAUGE),
                Effect.SetFlag(FlagId.AUGE_EINGESETZT),
                Effect.SetFlag(FlagId.FEUER_ENTZUENDET),
                Effect.PlaySfx("finale"),
                Effect.EndGame,
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_fassung", VerbId.BENUTZE, usedItem = ItemId.KRAKENAUGE,
            text = "Du setzt das Krakenauge in die Fassung. Es glimmt und pulsiert — aber der blinde Reflektor schluckt das Licht. Da fehlt noch Glanz.",
            effects = listOf(
                Effect.RemoveItem(ItemId.KRAKENAUGE),
                Effect.SetFlag(FlagId.AUGE_EINGESETZT),
                Effect.PlaySfx("pickup"),
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_spiegel", VerbId.BENUTZE, usedItem = ItemId.LAPPEN,
            condition = { it.has(FlagId.AUGE_EINGESETZT) },
            text = "Du polierst Salz und Jahre vom Reflektor. Kaum ist er blank, fängt er den Schein des Krakenauges — und wirft einen mächtigen Lichtkegel über die Bucht. Das Leuchtfeuer von Kap Krakenzahn BRENNT!",
            effects = listOf(
                Effect.SetFlag(FlagId.SPIEGEL_POLIERT),
                Effect.SetFlag(FlagId.FEUER_ENTZUENDET),
                Effect.PlaySfx("finale"),
                Effect.EndGame,
            ),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_spiegel", VerbId.BENUTZE, usedItem = ItemId.LAPPEN,
            text = "Du polierst den großen Reflektor, bis er blitzt. Jetzt fehlt nur noch das Licht, das er spiegeln soll.",
            effects = listOf(Effect.SetFlag(FlagId.SPIEGEL_POLIERT), Effect.PlaySfx("pickup")),
        ))
        add(Rule(
            LocationId.LEUCHTTURM, "turm_spiegel", VerbId.BENUTZE,
            text = "Mit dem Ärmel? Das schmiert mehr, als es putzt. Ein Lappen wäre gut.",
        ))
    }

    // ---- Inventar-Kombinationen ---------------------------------------------

    val combos: List<Combo> = listOf(
        Combo(
            ItemId.SANDDORNPUNSCH, ItemId.FEUERPFEFFER,
            "Du zerbröselst die Schote in den Punsch. Es zischt, dampft und riecht nach Abenteuer: Spezial-Punsch à la Rotbart!",
            listOf(
                Effect.RemoveItem(ItemId.SANDDORNPUNSCH), Effect.RemoveItem(ItemId.FEUERPFEFFER),
                Effect.AddItem(ItemId.SPEZIALPUNSCH), Effect.PlaySfx("erfolg"),
            ),
        ),
        Combo(
            ItemId.LATERNE, ItemId.TRANOEL,
            "Du füllst die Laterne mit Tranöl und entzündest den Docht. Warmes Licht — die Dunkelheit kann kommen.",
            listOf(
                Effect.RemoveItem(ItemId.LATERNE), Effect.RemoveItem(ItemId.TRANOEL),
                Effect.AddItem(ItemId.BRENNENDE_LATERNE), Effect.SetFlag(FlagId.LICHT_LATERNE),
                Effect.PlaySfx("erfolg"),
            ),
        ),
        Combo(
            ItemId.STOCK, ItemId.TEER,
            "Du knetest den Teer um das Astende. Eine ordentliche Teerfackel — fehlt nur noch ein Funke.",
            listOf(
                Effect.RemoveItem(ItemId.STOCK), Effect.RemoveItem(ItemId.TEER),
                Effect.AddItem(ItemId.TEERFACKEL), Effect.PlaySfx("pickup"),
            ),
        ),
        Combo(
            ItemId.TEERFACKEL, ItemId.FEUERSTEIN,
            "Funken sprühen, der Teer fängt Feuer: deine Fackel brennt!",
            listOf(
                Effect.RemoveItem(ItemId.TEERFACKEL),
                Effect.AddItem(ItemId.FACKEL), Effect.SetFlag(FlagId.LICHT_FACKEL),
                Effect.PlaySfx("erfolg"),
            ),
        ),
        Combo(
            ItemId.SEIL, ItemId.ENTERHAKEN,
            "Du vertäust den Enterhaken seemännisch am Seil. Ein Wurfanker, wie er im Piratenbuche steht.",
            listOf(
                Effect.RemoveItem(ItemId.SEIL), Effect.RemoveItem(ItemId.ENTERHAKEN),
                Effect.AddItem(ItemId.WURFANKER), Effect.PlaySfx("erfolg"),
            ),
        ),
    )

    // ---- Dialog-Whitelist ----------------------------------------------------

    /** Platzhalter-Id für Fielos wechselnde Ware. */
    const val FIELO_WARE = "FIELO_WARE"

    /**
     * Alles, was die KI (oder der Ersatz-Dialog) bei dieser Figur im
     * aktuellen Spielstand zugestehen darf: statische Whitelist der Figur
     * plus die ihr per Variation zugeteilten, noch nicht enthüllten
     * Wissens-Hinweise.
     */
    fun allowedEffectsFor(state: GameState, npcId: NpcId): List<AllowedEffect> {
        val v = variation(state)
        val statisch = npc(npcId).allowedEffects.filter { it.condition(state) }
        val hinweise = v.hintsOf(npcId)
            .filter { !state.knows(it) }
            .map { hint ->
                AllowedEffect(
                    AllowedEffect.TYP_HINWEIS, hint.name,
                    "verrät folgendes Wissen: ${Hints.text(hint, v)}",
                    frage = "Frag nach ${Hints.topic(hint)}.",
                    antwort = Hints.text(hint, v),
                )
            }
        return statisch + hinweise
    }

    /** Löst eine Item-Id aus der Whitelist auf (inkl. Fielo-Platzhalter). */
    fun resolveItem(state: GameState, id: String): ItemId? =
        if (id == FIELO_WARE) variation(state).fieloStock
        else runCatching { ItemId.valueOf(id) }.getOrNull()

    /**
     * Maras Amulett gibt es nur im Tausch. Fehlt in einer Effektliste die
     * Gegenleistung, wird sie ergänzt — bevorzugt das Medaillon.
     */
    fun enforceAmulettTausch(state: GameState, effects: List<Effect>): List<Effect> {
        val gibtAmulett = effects.any { it is Effect.AddItem && it.item == ItemId.GEZEITENAMULETT }
        val nimmtGlitzer = effects.any {
            it is Effect.RemoveItem && (it.item == ItemId.MEDAILLON || it.item == ItemId.SPIEGEL)
        }
        if (!gibtAmulett || nimmtGlitzer) return effects
        val glitzer = when {
            state.has(ItemId.MEDAILLON) -> ItemId.MEDAILLON
            state.has(ItemId.SPIEGEL) -> ItemId.SPIEGEL
            else -> return effects
        }
        return listOf(Effect.RemoveItem(glitzer)) + effects
    }

    /** Übersetzt ein Whitelist-Zugeständnis in anwendbare Effekte. */
    fun toEffects(state: GameState, npcId: NpcId, allowed: AllowedEffect): List<Effect> =
        when (allowed.typ) {
            AllowedEffect.TYP_HINWEIS ->
                runCatching { HintId.valueOf(allowed.id) }.getOrNull()
                    ?.let { listOf(Effect.RevealHint(it)) } ?: emptyList()
            AllowedEffect.TYP_FLAG ->
                runCatching { FlagId.valueOf(allowed.id) }.getOrNull()
                    ?.let { listOf(Effect.SetFlag(it)) } ?: emptyList()
            AllowedEffect.TYP_ITEM ->
                resolveItem(state, allowed.id)?.let { listOf(Effect.AddItem(it)) } ?: emptyList()
            AllowedEffect.TYP_ITEMWEG ->
                resolveItem(state, allowed.id)?.let { listOf(Effect.RemoveItem(it)) } ?: emptyList()
            AllowedEffect.TYP_DUELL -> listOf(Effect.StartDuel(npcId))
            AllowedEffect.TYP_RAETSEL -> listOf(Effect.StartRiddle)
            else -> emptyList()
        }

    // ---- Neues Spiel ---------------------------------------------------------

    fun newGame(seed: Long, name: String): GameState =
        GameState(name = name, seed = seed, locationId = LocationId.HAFEN, heroX = 0.35f)
}
