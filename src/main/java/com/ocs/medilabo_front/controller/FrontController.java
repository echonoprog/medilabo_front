package com.ocs.medilabo_front.controller;

import com.ocs.medilabo_front.beans.PatientBean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Arrays;

@Controller
public class FrontController {

    private final String backendUrl = "http://localhost:8080";
    private final String patientEndpoint = "/patients";
    private final RestTemplate restTemplate;

    public FrontController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/patientList")
    public String showAllPatients(Model model) {
        String endpoint = backendUrl + patientEndpoint;
        ResponseEntity<PatientBean[]> response = restTemplate.getForEntity(endpoint, PatientBean[].class);
        List<PatientBean> patients = Arrays.asList(response.getBody());
        model.addAttribute("patients", patients);
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
}