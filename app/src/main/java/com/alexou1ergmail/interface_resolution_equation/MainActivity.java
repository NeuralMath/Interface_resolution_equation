package com.alexou1ergmail.interface_resolution_equation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.SuperscriptSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private TextView TextViewReponse;
    private TextView demarche;
    private TextView TextViewEquation;

    private ArrayList<String> etapesText;           //String des étapes de résolution d'une équation mathématique
    private ArrayList<String> demarcheText ;        //String des explications par étapes de résolution d'une équation mathématique
    private ArrayList<ExpandableTextView> texViewList;  //Liste des textView de démarches (TextView Custom)
    private Spinner spinnerMethode;

    private LinearLayout linearDemarche;            //Layout des démarches

    private String equation = "f(7,5)= 2^2^(2) ";     //Équation reçue

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        demarche = (TextView) findViewById(R.id.textView2);
        TextViewReponse = (TextView) findViewById(R.id.textView4);
        TextViewEquation = (TextView) findViewById(R.id.textView8);
        linearDemarche = (LinearLayout) findViewById(R.id.LayoutVertical);

        texViewList = new ArrayList<ExpandableTextView>(10);


        demarcheText = new ArrayList<>();
        TextViewEquation.setText(equation);


        spinnerMethode = (Spinner) findViewById(R.id.spinner);

        List<String> spinnerArray =  new ArrayList<String>();
        equation = equation.replaceAll(" ", "");  //Donne une copie de l'équation sans les espaces


        for(int i=0;i<equation.length()-1;i++)    //Début de la normalisation des équations : mets un * entre les lettres et les chiffres ainsi que lettres et parenthèses ou chiffre + parenthèses
        {
            String chaineTemp = String.valueOf(equation.charAt(i))+String.valueOf(equation.charAt(i+1));

            if(Pattern.matches("(?i)\\d[a-z]",chaineTemp ) ||  Pattern.matches("//",chaineTemp )  || Pattern.matches("(?i)\\d\\(",chaineTemp ) || Pattern.matches("(?i)([a-e]|[h-m]|[o-r]|[t-z])\\(",chaineTemp ))
            {
                String chaine1 = equation.substring(0, i+1) + "*"; // On sépare le string en 2 à la position qui respecte les conditions.
                String chaine2 = equation.substring(i+1);
                equation = chaine1 + chaine2;
                System.out.println(equation);
            }
        }

        //Remplacer les symboles mathématiques par leur valeur approximative.
        equation = equation.replace("/PI/", String.valueOf(Math.PI));
        equation = equation.replace("/e/", String.valueOf(Math.exp(1)));



        if( equation.contains("//d"))            //Si l'équation contient un symbole de dérivée
        {
            spinnerArray.add(getString(R.string.Deriver));

        }
        else if( equation.contains("//S"))       //Si l'équation contient un symbole d'intégrale
        {
            spinnerArray.add(getString(R.string.Integrer));
        }
        else if(Pattern.matches("(?i)(f\\(([a-e]|[h-m]|[o-r]|[t-z])\\).*) | (y.*)", equation)) // si f(x) ou y //https://regex101.com/
        {
            spinnerArray.add(getString(R.string.Simplification));
            spinnerArray.add(getString(R.string.Factorisation));
            spinnerArray.add(getString(R.string.TrouverZéros));
        }
        else if(Pattern.matches("(?i)\\w\\(\\-?\\d+(\\,\\d*)?\\)=.*", equation)) // si f( ? ) //https://regex101.com/ //Remplacer x pour trouver y
        {
            spinnerArray.add(getString(R.string.TrouverY));
        }
        else if(Pattern.matches("\\-?\\d+(\\,\\d*)?.*", equation) && (!equation.contains("f(x)"))&&(!equation.contains("y"))) // si ? = x   //isoler x
        {
            spinnerArray.add(getString(R.string.TrouverX));
        }
        else          //Aucune méthode de résolution possible.
        {
            spinnerArray.add("0");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMethode.setAdapter(adapter);


        //link des clics à une méthode dans le spinner de choix de méthode.
        spinnerMethode.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

                        Object item = parent.getItemAtPosition(pos);
                        System.out.println(item.toString());     //Montre en console le choix pris

                        if(item.toString() == "0") TextViewReponse.setText("Aucune réponse possible \n(0 méthode de résolution)");
                        else if(item.toString() == getString(R.string.TrouverY))trouverY();
                        else if(item.toString() == getString(R.string.TrouverX))trouverX();
                        else if(item.toString() == getString(R.string.TrouverZéros))trouverZeros();
                        else if(item.toString() == getString(R.string.Simplification))simplification();
                        else if(item.toString() == getString(R.string.Factorisation)) factorisation();
                        else if(item.toString() == getString(R.string.Deriver))deriver();
                        else if(item.toString() == getString(R.string.Integrer))integrer();
                    }
                    public void onNothingSelected(AdapterView<?> parent) {}
                });
    }
    public void ajouterEtapes()     //Méthode qui sert à ajouter les étapes dans les textView custom en enlevant les précédents. (utilisée pour vider )
    {
        texViewList.clear();
        linearDemarche.removeAllViews();
        linearDemarche.addView(demarche);
    }
    public void ajouterEtapes(ArrayList<String> m_demarche , String firstHalfEquation, ArrayList<Integer> m_EtapesGrasI,ArrayList<Integer> m_EtapesGrasF)//Méthode qui sert à ajouter les étapes dans les textView custom en enlevant les précédents.
    {
        demarcheText = m_demarche;
        texViewList.clear();
        linearDemarche.removeAllViews();
        linearDemarche.addView(demarche);
        int nbDeX = 0;


        for(int i = 0; i < demarcheText.size()-1; i++) {        //Pour toutes les étapes ;
            ExpandableTextView demarcheTextView = new ExpandableTextView(this);
            if(demarcheText.get(i).contains("^"))               //Si l'étape contient un exposant
            {
                int jDebut = demarcheText.get(i).indexOf('^') + firstHalfEquation.length();
                int jFin ;
                int Parentheses = 0;
                SpannableStringBuilder expString = new SpannableStringBuilder( firstHalfEquation + demarcheText.get(i) + "\n" + etapesText.get(i) + "\n           ----------------------------------");
                if(i==0)
                {
                    Matcher m = Pattern.compile("x").matcher(demarcheText.get(0)); //http://stackoverflow.com/questions/8938498/get-the-index-of-a-pattern-in-a-string-using-regex
                    while (m.find())
                    {
                        mettreEnGrasEtape(m_EtapesGrasI.get(nbDeX)+firstHalfEquation.length(),m_EtapesGrasF.get(nbDeX)+firstHalfEquation.length(),expString);
                        nbDeX++;
                    }
                    nbDeX--;
                }
                else mettreEnGrasEtape(m_EtapesGrasI.get(i+nbDeX)+firstHalfEquation.length(),m_EtapesGrasF.get(i+nbDeX)+firstHalfEquation.length(),expString);

                while (jDebut != -1)        //Tant que l'étape contient un exposant
                {
                    String stringRemplace ="";
                    if( expString.charAt(jDebut+1) != '(')      //S'il n'y a pas une parenthèse après le ^
                    {
                        jFin = jDebut+1;
                        while(Character.isDigit(expString.charAt(jFin+1)) || expString.charAt(jFin+1) =='.')    jFin++;

                        for(int k = 1; k <= jFin - jDebut; k++ ) stringRemplace += expString.charAt(jDebut+k);

                        expString = expString.replace(jDebut,jFin+1,stringRemplace);

                        jDebut --;
                        jFin --;
                        expString.setSpan(new SuperscriptSpan(), jDebut+1, jFin+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        expString.setSpan(new RelativeSizeSpan(0.75f), jDebut+1, jFin+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    }else{                  //S'il y a une parenthèse après le ^
                        jFin = jDebut;
                        int charDebut = jDebut + 2;         //Caractere situé plus loin que le ^ et la (
                        do {        //Compter les parenthèses au cas où de multiples ^
                            jFin++;
                            char tempChar = expString.charAt(jFin);
                            if(tempChar =='(') Parentheses ++;
                            else if(tempChar == ')') Parentheses --;
                            if (jFin==expString.length()-1)break;
                        }while(Parentheses > 0);


                        while(charDebut != jFin)
                        {
                            stringRemplace += expString.charAt(charDebut) ;
                            charDebut++;
                        }

                        expString = expString.replace(jDebut-1,jFin+1,stringRemplace);

                        jDebut -= 2;
                        jFin -= 2;
                        expString.setSpan(new SuperscriptSpan(), jDebut+1, jFin+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        expString.setSpan(new RelativeSizeSpan(0.75f), jDebut+1, jFin+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    }

                            jDebut = -1;
                            for(int k  = 0; k < expString.length()-1;k++ )
                            {
                                if(expString.charAt(k)=='^')
                                {
                                    jDebut = k;
                                    break;
                                }
                            }


                }

                demarcheTextView.setText(expString);
            }
            else
            {
                SpannableStringBuilder expString = new SpannableStringBuilder( firstHalfEquation + demarcheText.get(i) + "\n" + etapesText.get(i) + "\n            ----------------------------------");
                if(i==0)
                {
                    Matcher m = Pattern.compile("x").matcher(demarcheText.get(0)); //http://stackoverflow.com/questions/8938498/get-the-index-of-a-pattern-in-a-string-using-regex
                    while (m.find())
                    {
                        mettreEnGrasEtape(m_EtapesGrasI.get(nbDeX)+firstHalfEquation.length(),m_EtapesGrasF.get(nbDeX)+firstHalfEquation.length(),expString);
                        nbDeX++;
                    }
                    nbDeX--;
                }
                else { mettreEnGrasEtape(m_EtapesGrasI.get(i+nbDeX)+firstHalfEquation.length(),m_EtapesGrasF.get(i+nbDeX)+firstHalfEquation.length(),expString); }




                demarcheTextView.setText(expString);
            }

            linearDemarche.addView(demarcheTextView);

            texViewList.add(demarcheTextView);
        }
        ExpandableTextView reponseTextView = new ExpandableTextView(this);
        reponseTextView.setText(firstHalfEquation + " " + TextViewReponse.getText());
        linearDemarche.addView(reponseTextView);
        texViewList.add(reponseTextView);
    }

    /**
     *
     * donction qui sert à mettre en gras une étape de résolution
     * @param debut
     * @param fin
     * @param m_string
     */
    public void mettreEnGrasEtape(int debut, int fin, SpannableStringBuilder m_string)
    {
        StyleSpan b = new StyleSpan(android.graphics.Typeface.BOLD); //http://stackoverflow.com/questions/20850822/making-part-of-a-string-bold-in-textview
        m_string.setSpan(b,debut,fin,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    /**
     * Fonction qui résout une équation si l'équation a une valeur de x donnée
     */
    public void trouverY()
    {
        TrouverY resolutionY = new TrouverY(equation);
        TextViewReponse.setText(String.valueOf(resolutionY.getY()));
        demarche.setText("DÉMONSTRATION : Trouver y\n ");
        etapesText = resolutionY.getM_EtapesText();
        ajouterEtapes(resolutionY.getM_DemarcheText(),resolutionY.getM_FirstEquationHalf(),resolutionY.getM_EtapesGrasI(),resolutionY.getM_EtapesGrasF());
    }
    public void trouverX()
    {
        demarche.setText("DÉMONSTRATION : Trouver x\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
    public void trouverZeros()
    {
        demarche.setText("DÉMONSTRATION : Trouver zeros\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
    public void simplification()
    {
        demarche.setText("DÉMONSTRATION : Trouver simplification\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
    public void factorisation()
    {
        demarche.setText("DÉMONSTRATION : factoriser\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
    public void deriver()
    {
        demarche.setText("DÉMONSTRATION : deriver\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
    public void integrer()
    {
        demarche.setText("DÉMONSTRATION : integrer\n");
        TextViewReponse.setText("Erreur 404");
        demarcheText = new ArrayList<>(0);
        ajouterEtapes();
    }
}
