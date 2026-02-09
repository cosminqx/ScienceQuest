import java.util.ArrayList;
import java.util.List;

/**
 * QuestionPools - Centralized storage for educational questions by topic
 */
public class QuestionPools
{
    public static List<DialogueQuestion> getBiologyQuestions()
    {
        List<DialogueQuestion> questions = new ArrayList<>();
        
        questions.add(new DialogueQuestion(
            "biology",
            "Care este procesul prin care plantele produc hrană folosind lumina soarelui?",
            new String[] { "Fotosinteza", "Respirația", "Fermentația", "Digestia" },
            0,
            "Corect! Fotosinteza este procesul vital pentru plante.",
            "Greșit. Răspunsul corect este Fotosinteza. Mai încearcă!"
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Care este unitatea de bază a vieții?",
            new String[] { "Celula", "Atomul", "Molecula", "Țesutul" },
            0,
            "Excelent! Celula este unitatea fundamentală a vieții.",
            "Nu e corect. Răspunsul este Celula. Încearcă din nou!"
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Ce organ pompează sângele prin corpul uman?",
            new String[] { "Inima", "Plămânul", "Ficatul", "Rinichii" },
            0,
            "Corect! Inima pompează sângele prin tot corpul.",
            "Greșit. Inima este organul care pompează sângele."
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Care este molecula care poartă informația genetică?",
            new String[] { "ADN", "ARN", "Proteina", "Lipida" },
            0,
            "Perfect! ADN-ul conține codul genetic.",
            "Nu e corect. ADN-ul este răspunsul corect."
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Ce proces permite organismelor să transforme hrana în energie?",
            new String[] { "Respirația celulară", "Fotosinteza", "Digestia", "Reproducția" },
            0,
            "Corect! Respirația celulară produce energie.",
            "Greșit. Răspunsul este respirația celulară."
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Care sunt cele două tipuri principale de celule?",
            new String[] { "Procariote și eucariote", "Animale și vegetale", "Vii și moarte", "Mici și mari" },
            0,
            "Excelent! Procariote (fără nucleu) și eucariote (cu nucleu).",
            "Nu e corect. Sunt procariote și eucariote."
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Ce găsim în interior celulelor care produce energia?",
            new String[] { "Mitocondriile", "Ribozomii", "Nucleul", "Membrana" },
            0,
            "Perfect! Mitocondriile sunt centralele energetice ale celulei.",
            "Greșit. Mitocondriile produc energia."
        ));
        
        questions.add(new DialogueQuestion(
            "biology",
            "Ce sistem controlează toate funcțiile corpului?",
            new String[] { "Sistemul nervos", "Sistemul digestiv", "Sistemul circulator", "Sistemul respirator" },
            0,
            "Corect! Sistemul nervos coordonează toate funcțiile.",
            "Nu e corect. Sistemul nervos este răspunsul."
        ));
        
        return questions;
    }
    
    public static List<DialogueQuestion> getPhysicsQuestions()
    {
        List<DialogueQuestion> questions = new ArrayList<>();
        
        questions.add(new DialogueQuestion(
            "physics",
            "Care este unitatea de măsură pentru forță în sistemul internațional?",
            new String[] { "Newton", "Joule", "Watt", "Pascal" },
            0,
            "Corect! Newton este unitatea pentru forță.",
            "Greșit. Răspunsul corect este Newton (N). Mai încearcă!"
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Ce tip de energie are un obiect în mișcare?",
            new String[] { "Cinetică", "Potențială", "Termică", "Chimică" },
            0,
            "Excelent! Energia cinetică este energia mișcării.",
            "Nu e corect. Energia cinetică este răspunsul corect."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Care este viteza luminii în vid?",
            new String[] { "300,000 km/s", "150,000 km/s", "500,000 km/s", "1,000,000 km/s" },
            0,
            "Perfect! Lumina călătorește la aproximativ 300,000 km/s.",
            "Greșit. Viteza luminii este aproximativ 300,000 km/s."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Ce lege afirmă că un obiect în repaus rămâne în repaus dacă nu acționează o forță?",
            new String[] { "Prima lege a lui Newton", "A doua lege a lui Newton", "A treia lege a lui Newton", "Legea gravitației" },
            0,
            "Corect! Aceasta este legea inerției (Prima lege a lui Newton).",
            "Nu e corect. Este Prima lege a lui Newton (inerția)."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Ce tip de curent electric circulă în direcție constantă?",
            new String[] { "Curent continuu", "Curent alternativ", "Curent static", "Curent magnetic" },
            0,
            "Perfect! Curentul continuu circulă într-o direcție constantă.",
            "Greșit. Curentul continuu este răspunsul corect."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Care este unitatea pentru măsurarea puterii electrice?",
            new String[] { "Watt", "Volt", "Amper", "Ohm" },
            0,
            "Corect! Watt-ul măsoară puterea electrică.",
            "Nu e corect. Răspunsul este Watt."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Ce fenomen explica de ce vedem curcubeul?",
            new String[] { "Reflexia și refracția luminii", "Absorbția luminii", "Difracția luminii", "Polarizarea luminii" },
            0,
            "Excelent! Lumina se refractă prin picaturile de apă creând curcubeul.",
            "Greșit. Reflexia și refracția sunt răspunsul corect."
        ));
        
        questions.add(new DialogueQuestion(
            "physics",
            "Ce forta ține planetele în orbita lor?",
            new String[] { "Gravitația", "Magnetismul", "Fricțiunea", "Tensiunea" },
            0,
            "Perfect! Gravitația ține planetele în orbită în jurul Soarelui.",
            "Nu e corect. Gravitația este răspunsul corect."
        ));
        
        return questions;
    }
    
    public static List<DialogueQuestion> getGeneralScienceQuestions()
    {
        List<DialogueQuestion> questions = new ArrayList<>();
        
        questions.add(new DialogueQuestion(
            "general",
            "Care este cea mai mică formă a universului?",
            new String[] { "Atom", "Moleculă", "Celulă", "Galaxie" },
            0,
            "Corect! Tot în univers este construit din atomi. Bravo!",
            "Nu chiar. Răspunsul corect este atomul. Vom acoperi asta mai detaliat curând."
        ));
        
        questions.add(new DialogueQuestion(
            "general",
            "Ce gaz respiră plantele în timpul zilei?",
            new String[] { "Dioxid de carbon", "Oxigen", "Azot", "Hidrogen" },
            0,
            "Perfect! Plantele absorb CO₂ și eliberează oxigen.",
            "Greșit. Plantele absorb dioxid de carbon (CO₂)."
        ));
        
        questions.add(new DialogueQuestion(
            "general",
            "Ce planetă este cea mai apropiată de Soare?",
            new String[] { "Mercur", "Venus", "Pământ", "Marte" },
            0,
            "Corect! Mercur este cea mai apropiată planetă de Soare.",
            "Nu e corect. Mercur este cel mai aproape de Soare."
        ));
                questions.add(new DialogueQuestion(
            "general",
            "Ce stare de agregare are apa la temperatura camerei?",
            new String[] { "Lichidă", "Solidă", "Gazoasă", "Plasma" },
            0,
            "Corect! Apa este lichidă la temperatura camerei.",
            "Greșit. Apa este lichidă la temperatura normală."
        ));
        
        questions.add(new DialogueQuestion(
            "general",
            "Din ce este format nucleul atomului?",
            new String[] { "Protoni și neutroni", "Electroni și protoni", "Numai neutroni", "Numai protoni" },
            0,
            "Perfect! Nucleul conține protoni și neutroni.",
            "Nu e corect. Protoni și neutroni formează nucleul."
        ));
        
        questions.add(new DialogueQuestion(
            "general",
            "Care este formula chimică a apei?",
            new String[] { "H₂O", "CO₂", "O₂", "H₂SO₄" },
            0,
            "Excelent! Apa este H₂O - doi atomi de hidrogen și unul de oxigen.",
            "Greșit. Răspunsul corect este H₂O."
        ));
        
        questions.add(new DialogueQuestion(
            "general",
            "Ce tip de energie produce Soarele?",
            new String[] { "Energie nucleară", "Energie chimică", "Energie mecanică", "Energie electrică" },
            0,
            "Perfect! Soarele produce energie prin fuziune nucleară.",
            "Nu e corect. Soarele produce energie nucleară."
        ));
                return questions;
    }
}
