# IDATA2304 – Smart-Farm Gruppeprosjekt (Fall 2025)

Dette dokumentet er en oversettelse og omstrukturering av prosjektbeskrivelsen.  
Formålet er å gi full kontekst direkte i GitHub/IDE for bruk med AI-verktøy som Copilot.

---

## Introduksjon
Prosjektet er en del av IDATA2304 *Computer Communication and Network Programming*.  
Målet er å lære nettverksprogrammering i praksis gjennom å lage en komplett smart farming-løsning.  

Systemet består av:
- **Sensor-/aktuatornoder**: samler inn data (f.eks. temperatur, luftfuktighet, lys) og styrer aktuatorer (f.eks. vifter, varme, vinduer).  
- **Kontrollpanelnoder**: viser data og sender kommandoer.  

All kommunikasjon skjer via nettverkssockets. Ingen fysisk hardware skal lages, alt er simulert.

---

## Hovedoppgave
Implementer en fullstendig applikasjon med:
1. **Eget applikasjonslagsprotokoll** (design + implementasjon).  
2. **Dokumentasjon av protokollen** i `protocol.md`.  
3. **Implementasjon av noder og evt. server**.  
4. **Kvalitetssikret kildekode** (clean code, struktur, git-bruk).  
5. **Prosessdokumentasjon** (sprint-rapporter, roller, retrospektiv).  
6. **Video-presentasjon** (10–15 min).  

---

## Krav til protokollen
- Må bygges direkte på TCP **eller** UDP (ingen HTTP/MQTT eller andre høy-nivå protokoller).  
- Skal støtte flere sensor-noder og kontrollpaneler.  
- Må være skalerbar: nye sensortyper og aktuatortyper kan legges til.  
- Skal støtte flere kommandoer (ikke forhåndsdefinert liste).  
- Kommandoer sendes fra **kontrollpanel** til spesifikk sensor-node.  
- Protokollen må definere informasjonsflyt (pull/push/publish-subscribe).  
- Skal håndtere feilsituasjoner og ugyldige meldinger.  

---

## Krav til sensor-noder
- Har en unik ID (adresse).  
- Kan ha flere sensorer (minst én av hver type).  
- Kan også ha flere aktuatorer.  
- Skal sende sensordata og rapportere status på aktuatorer.  
- Skal kunne motta og utføre kommandoer.  

---

## Krav til kontrollpanel-noder
- Vise en liste over alle sensornoder.  
- Motta og visualisere sensordata (tekst er nok, grafikk valgfritt).  
- Motta og vise aktuatorstatus (på/av, åpen/lukket osv.).  
- Sende kommandoer til sensornoder.  

---

## Krav til protokollbeskrivelsen (`protocol.md`)
Protokollen må dokumenteres med:
- Introduksjon og terminologi.  
- Valgt transport (TCP/UDP) + portnummer.  
- Arkitektur (roller, klient/server, meldingsflyt).  
- Protokolltype (connection-oriented/less, stateful/stateless).  
- Typer og konstanter.  
- Meldingsformat (typer, marshalling).  
- Feilhåndtering og respons.  
- Eksempelscenario (brukeropplevelse + meldinger).  
- Eventuelle sikkerhets- og pålitelighetsmekanismer.  
- Begrunnelse for designvalg.  

---

## Krav til implementasjon
- Sensor-/aktuatornoder.  
- Kontrollpanelnoder.  
- Eventuell server.  
- Må håndtere:
  - Tilkobling mellom noder.  
  - Mottak og sending av sensordata.  
  - Videresending av kommandoer til riktige noder/aktuatorer.  
  - Visualisering av data på kontrollpanel.  
  - Feilhåndtering og tilkoblingsfeil.  

---

## Krav til kodekvalitet
- Følg clean code-prinsipper (struktur, ansvar, ingen duplisering).  
- Bruk Git aktivt gjennom hele prosjektet.  
- Tydelig mappestruktur (ingen "finalProject_v2" etc.).  
- Referer alltid til kilde hvis kode er kopiert eller generert.  

---

## Krav til prosessdokumentasjon
- Planlegging med sprints (iterasjoner).  
- Fordeling av oppgaver (alle må bidra til nettverksdelen).  
- Bruk av issues (små, tydelige, definition of done).  
- Sprint-rapporter og retrospektiv.  
- Sprintdokumentasjon bør ligge i repoet (wiki/md).  

---

## Krav til video-presentasjon
Lengde: 10–15 minutter, på engelsk. Innhold:  
1. Introduksjon (hvilket problem løses).  
2. Tilnærming og research.  
3. Arbeidsprosess (sprints, roller).  
4. Arkitektur (noder, roller, diagrammer).  
5. Protokoll (hovedidé og meldingsformat).  
6. Demo av løsning.  
7. Eventuelle ekstra-funksjoner.  
8. Refleksjon og forbedringsmuligheter.  

---

## Mulige ekstra-funksjoner (for bedre karakter)
- Resiliens ved nettverksfeil (buffer, reconnect, retransmisjon).  
- Kryptering/sikkerhet.  
- Automatisk ID-tildeling (inspirasjon: DHCP).  
- Bilde-/filoverføring som sensordata.  
- Broadcast/multicast-kommandoer.  
- Støtte for ulike datagrader (minutt-, timegjennomsnitt osv.).  

---

## Innlevering
- **Frist:** 21. november 2025, kl. 12:00 (Inspera).  
- **Leveransen skal inneholde:**
  1. GitHub-lenke med kildekode og dokumentasjon.  
  2. Mapping kandidatnummer ↔ GitHub-brukernavn.  
  3. Lenke til `protocol.md`.  
  4. Lenker til sprint-rapporter.  
  5. Video (mp4 eller annet vanlig format).  

---

## Vurdering
- Oppfyller alle fundamentale krav → C-nivå.  
- Ekstra funksjoner + god kvalitet → B/A-nivå.  
- Mulighet for individuelle karakterer dersom bidrag varierer.  

---
