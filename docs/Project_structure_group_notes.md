# Prosjektstruktur (internt notat)

Dette er en kort forklaring på hvorfor prosjektet er satt opp slik det er.     
Dette dokumentet er kun ment for oss i gruppa.  
  
Etter å ha lest over oppgaven noen ganger og diskutert den med ChatGPT, så var denne måten å gjøre det på mest anbefalt. Vi får gjøre noe nytt (Multi-modul) og ting blir ryddig når man blir vandt med strukturen. Alternativet var å ha alt klistret i samme struktur, men det kan fort bli rotete om vi skal opprettholde gode designprinsipper.

---

## Hvorfor multi-modul?
Oppgaven krever at vi lager flere **applikasjoner** som kjører samtidig og snakker sammen:
- **Sensor-node**
- **Control-panel**
- **Broker** (mellomtjener)

For å håndtere dette ryddig bruker vi **multi-modul Maven**:
- Hver applikasjon ligger i sin egen modul.
- Felles kode (protokoll, meldingsformater, transport, utils) ligger i `common`.
- Parent-POM styrer Java-versjon og felles plugin-konfig.

Fordeler:
- Klart skille mellom applikasjonene.
- Ryddigere testing (vi kan teste logikk uten å starte hele systemet).
- Lett å bygge og kjøre hver applikasjon separat.

---

## Mappestruktur

```text
smart-farm/                <-- rotprosjekt (parent POM)
├── pom.xml                <-- parent (styrer versjoner, plugins)
├── protocol.md            <-- beskriver egen applikasjonsprotokoll
├── docs/                  <-- dokumentasjon.
│
├── common/                <-- felles kode (protokoll, transport, util)
│
├── sensor-node/           <-- program for sensorer/aktuatorer
│
├── control-panel/         <-- program for bondens kontrollpanel
│
└── broker/                <-- program for mellomtjener
```

Alle moduler har `src/main/java` og `src/test/java`.  
Vi skiller koden inn i pakker:
- `entity` → domeneobjekter
- `logic` → logikk
- `infra` → infrastruktur (nettverksklienter, servere, IO)
- `app` → kun startklasse (`Main`)

---

## POM-filer
- **Parent POM** i rot:
  - Setter Java-versjon (JDK 21 med `<release>`).
  - Definerer felles plugins (compiler, surefire, exec).
  - Definerer modulene.
- **Modul-POMs**:
  - Har egne dependencies.
  - Angir `mainClass` for kjørbare apper.
  - `common` har ingen `mainClass`.

**Viktig å huske:**
- Legg dependencies i riktig modul.
- Spesifiser versjoner.
- Vær sikker på om noe skal i **pom (rot)** eller **pom (modul)** før man legger inn noe.

---

## Kjøre Main filene fra terminal
```text
mvn -pl broker exec:java
mvn -pl sensor-node exec:java
mvn -pl control-panel exec:java
```