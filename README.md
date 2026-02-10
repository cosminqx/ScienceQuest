![Stats Badge](https://hackatime-badge.hackclub.com/U0A9CUR24GJ/ScienceQuest)

# ScienceQuest

An educational 2D top-down RPG game developed in Java using Greenfoot, created by two high school students for the National Competition "Java – Programează în Greenfoot" organized by ADFABER.org and sponsored by Oracle Academy.

## About the Game

ScienceQuests simplifies science concepts through interactive gameplay. Players engage with NPCs (teachers and scientists) who explain concepts and present educational quizzes. Correct answers reward points, XP, badges, and progression through the game.

**Features:**
- Pixel-art style inspired by Stardew Valley
- Science topics: Biology, Chemistry, Physics, Environmental Science
- Interactive quests and NPC interactions
- Collision detection and keyboard controls
- Educational focus with engaging gameplay

This project demonstrates object-oriented programming principles in Greenfoot, emphasizing clean code, readability, and educational value.

## Documentatie (RO)

### Flux de joc
- Ecran de start: titlu -> introducere nume -> selectie gen.
- MainMapWorld: profesorul din clasa ofera intrebari; dupa suficient raspunsuri corecte se deblocheaza mini-quest-urile.
- Laboratoare: Biologie -> Fizica -> Chimie, fiecare cu intrebari NPC si mini-quest-uri specifice.
- Final: dupa completarea tuturor laboratoarelor si mini-quest-urilor din harta principala.

### Controale
- Miscare: sageti.
- Dialog: ENTER pentru avansare, ESC pentru inchidere.
- Intrebari: 1-4 sau sageti sus/jos pentru selectie, ENTER pentru confirmare.
- Mini-quest-uri: SPACE pentru pornire/actiune, plus sageti in functie de mecanica fiecarui quest.

### Harta si coliziuni
- Hartile sunt incarcate din TMJ/TMX si randate pe straturi.
- Coliziunile sunt bazate pe dreptunghiuri din layer-ul de collision.
- Pozitionarea si tranzitiile intre lumi se fac in functie de coordonatele jucatorului.
