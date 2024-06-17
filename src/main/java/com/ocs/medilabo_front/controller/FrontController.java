package com.ocs.medilabo_front.controller;

import com.ocs.medilabo_front.beans.NoteBean;
import com.ocs.medilabo_front.beans.PatientBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


import javax.validation.Valid;

@Controller
public class FrontController {

    private final String backendUrl = "http://medilabo-gateway:8080";
    private final String patientEndpoint = "/patients";
    private final String noteEndpoint = "/notes";
    private final String riskEndpoint = "/risk";
    private final RestTemplate restTemplate;

    public FrontController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/patientList")
    public String showAllPatients(Model model) {
        String endpoint = backendUrl + patientEndpoint;
        ResponseEntity<PatientBean[]> response = restTemplate.getForEntity(endpoint, PatientBean[].class);
        List<PatientBean> patients = Arrays.asList(response.getBody());

        // Récupérer le niveau de risque pour chaque patient
        List<String> riskLevels = new ArrayList<>();
        for (PatientBean patient : patients) {
            String riskLevel = restTemplate.getForObject(backendUrl + riskEndpoint + "/{id}", String.class, patient.getId());
            riskLevels.add(riskLevel);
        }

        model.addAttribute("patients", patients);
        model.addAttribute("riskLevels", riskLevels);
        return "patientList";
    }

    @GetMapping("/updatePatient/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        String endpoint = backendUrl + patientEndpoint + "/" + id;
        ResponseEntity<PatientBean> response = restTemplate.getForEntity(endpoint, PatientBean.class);
        PatientBean patient = response.getBody();
        model.addAttribute("patient", patient);
        return "updatePatient";
    }

    @PostMapping("/updatePatient/{id}")
    public String processUpdateForm(@PathVariable Long id, @ModelAttribute PatientBean patient) {
        String endpoint = backendUrl + patientEndpoint + "/" + id;
        restTemplate.put(endpoint, patient);
        return "redirect:/patientList";
    }

    @GetMapping("/addPatient")
    public String showAddPatientForm(Model model) {
        model.addAttribute("newPatient", new PatientBean());
        return "addPatient";
    }

    @PostMapping("/addPatient")
    public String processAddPatientForm(@Valid @ModelAttribute PatientBean newPatient, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("newPatient", newPatient);
            return "addPatient";
        } else {
            String endpoint = backendUrl + patientEndpoint;
            restTemplate.postForObject(endpoint, newPatient, PatientBean.class);
            return "redirect:/patientList";
        }
    }

    @GetMapping("/deletePatient/{id}")
    public String deletePatient(@PathVariable Long id) {
        String endpoint = backendUrl + patientEndpoint + "/" + id;
        restTemplate.delete(endpoint);
        return "redirect:/patientList";
    }

    @GetMapping("/noteList")
    public String showAllNotes(Model model) {
        String noteEndpoint = backendUrl + "/notes";
        ResponseEntity<NoteBean[]> response = restTemplate.getForEntity(noteEndpoint, NoteBean[].class);
        List<NoteBean> notes = Arrays.asList(response.getBody());
        model.addAttribute("notes", notes);
        return "noteList";
    }

    @GetMapping("/updateNote/{id}")
    public String showUpdateNoteForm(@PathVariable String id, Model model) {
        String endpoint = backendUrl + noteEndpoint + "/" + id;
        ResponseEntity<NoteBean> response = restTemplate.getForEntity(endpoint, NoteBean.class);
        NoteBean note = response.getBody();
        model.addAttribute("note", note);
        model.addAttribute("patId", note.getPatId());
        return "updateNote";
    }


    @PostMapping("/updateNote/{id}")
    public String processUpdateNoteForm(@PathVariable String id, @ModelAttribute NoteBean note, RedirectAttributes redirectAttributes) {
        String endpoint = backendUrl + noteEndpoint + "/" + id;
        restTemplate.put(endpoint, note);
        redirectAttributes.addFlashAttribute("confirmationMessage", "La note a été mise à jour avec succès.");
        return "redirect:/updateNote/" + id + "?patId=" + note.getPatId();
    }


    @GetMapping("/addNote/{id}")
    public String showAddNoteForm(@PathVariable Long id, Model model) {
        NoteBean newNote = new NoteBean();
        newNote.setPatId(id);

        // Récupérer les détails du patient
        String endpoint = backendUrl + patientEndpoint + "/" + id;
        ResponseEntity<PatientBean> patientResponse = restTemplate.getForEntity(endpoint, PatientBean.class);
        PatientBean patient = patientResponse.getBody();
        newNote.setPatient(patient.getNom());

        model.addAttribute("newNote", newNote);
        model.addAttribute("patient", patient);
        return "addNote";
    }

    @PostMapping("/addNote/{id}")
    public String processAddNoteForm(@PathVariable Long id, @ModelAttribute("newNote") NoteBean formNote, @RequestParam String patientName, RedirectAttributes redirectAttributes) {
        NoteBean newNote = new NoteBean();
        newNote.setPatId(id);
        newNote.setPatient(patientName);
        newNote.setNote(formNote.getNote());

        String endpoint = backendUrl + noteEndpoint;
        restTemplate.postForObject(endpoint, newNote, NoteBean.class);
        redirectAttributes.addFlashAttribute("confirmationMessage", "La note a été ajoutée avec succès.");
        return "redirect:/noteList/patient/" + id;
    }



    @GetMapping("/deleteNote/{id}")
    public String deleteNote(@PathVariable String id, @RequestParam Long patId, RedirectAttributes redirectAttributes) {
        String endpoint = backendUrl + noteEndpoint + "/" + id;
        restTemplate.delete(endpoint);
        redirectAttributes.addFlashAttribute("confirmationMessage", "La note a été supprimée avec succès.");
        return "redirect:/noteList/patient/" + patId;
    }


    @GetMapping("/noteList/patient/{patId}")
    public String getNotesByPatientId(@PathVariable Long patId, Model model) {
        String endpoint = backendUrl + noteEndpoint + "/patient/" + patId;
        ResponseEntity<NoteBean[]> response = restTemplate.getForEntity(endpoint, NoteBean[].class);
        List<NoteBean> notes = Arrays.asList(response.getBody());
        model.addAttribute("notes", notes);
        model.addAttribute("patId", patId);
        return "noteListByPatient";
    }

    @GetMapping("/riskLevel/{id}")
    public String getRiskLevel(@PathVariable Long id) {
        String riskLevel = restTemplate.getForObject(backendUrl + riskEndpoint + "/{id}", String.class, id);
        return riskLevel;
    }


}