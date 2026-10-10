package com.devops.tracker.web.ui;

import com.devops.tracker.domain.DeploymentStatus;
import com.devops.tracker.domain.EnvironmentType;
import com.devops.tracker.dto.DeploymentRequest;
import com.devops.tracker.dto.ProjectRequest;
import com.devops.tracker.exception.DuplicateResourceException;
import com.devops.tracker.service.DeploymentService;
import com.devops.tracker.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DashboardController {

    private final ProjectService projectService;
    private final DeploymentService deploymentService;

    public DashboardController(ProjectService projectService, DeploymentService deploymentService) {
        this.projectService = projectService;
        this.deploymentService = deploymentService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("projects", projectService.listProjects());
        model.addAttribute("pageTitle", "Deployment dashboard");
        return "dashboard";
    }

    @GetMapping("/projects/new")
    public String newProject(Model model) {
        model.addAttribute("project", new ProjectRequest("", "", "", ""));
        model.addAttribute("formAction", "/projects");
        model.addAttribute("pageTitle", "New project");
        return "project-form";
    }

    @PostMapping("/projects")
    public String createProject(
            @Valid @ModelAttribute("project") ProjectRequest project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/projects");
            model.addAttribute("pageTitle", "New project");
            return "project-form";
        }
        try {
            projectService.createProject(project);
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("project.create", ex.getMessage());
            model.addAttribute("formAction", "/projects");
            model.addAttribute("pageTitle", "New project");
            return "project-form";
        }
        redirectAttributes.addFlashAttribute("notice", "Project created.");
        return "redirect:/";
    }

    @GetMapping("/projects/{id}")
    public String projectDetail(@PathVariable Long id, Model model) {
        model.addAttribute("project", projectService.getProject(id));
        model.addAttribute("deployments", deploymentService.listByProject(id));
        model.addAttribute("environments", EnvironmentType.values());
        model.addAttribute("statuses", DeploymentStatus.values());
        model.addAttribute("deploymentForm", new DeploymentRequest("", null, DeploymentStatus.PENDING, null, "", ""));
        model.addAttribute("pageTitle", "Project details");
        return "project-detail";
    }

    @GetMapping("/projects/{id}/edit")
    public String editProject(@PathVariable Long id, Model model) {
        var project = projectService.getProject(id);
        model.addAttribute("project", new ProjectRequest(
                project.name(),
                project.description(),
                project.repositoryUrl(),
                project.owner()
        ));
        model.addAttribute("formAction", "/projects/" + id);
        model.addAttribute("pageTitle", "Edit project");
        return "project-form";
    }

    @PostMapping("/projects/{id}")
    public String updateProject(
            @PathVariable Long id,
            @Valid @ModelAttribute("project") ProjectRequest project,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formAction", "/projects/" + id);
            model.addAttribute("pageTitle", "Edit project");
            return "project-form";
        }
        try {
            projectService.updateProject(id, project);
        } catch (DuplicateResourceException ex) {
            bindingResult.reject("project.update", ex.getMessage());
            model.addAttribute("formAction", "/projects/" + id);
            model.addAttribute("pageTitle", "Edit project");
            return "project-form";
        }
        redirectAttributes.addFlashAttribute("notice", "Project updated.");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/projects/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projectService.deleteProject(id);
        redirectAttributes.addFlashAttribute("notice", "Project deleted.");
        return "redirect:/";
    }

    @PostMapping("/projects/{id}/deployments")
    public String recordDeployment(
            @PathVariable Long id,
            @Valid @ModelAttribute("deploymentForm") DeploymentRequest deploymentForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(id));
            model.addAttribute("deployments", deploymentService.listByProject(id));
            model.addAttribute("environments", EnvironmentType.values());
            model.addAttribute("statuses", DeploymentStatus.values());
            model.addAttribute("pageTitle", "Project details");
            return "project-detail";
        }
        deploymentService.recordDeployment(id, deploymentForm);
        redirectAttributes.addFlashAttribute("notice", "Deployment recorded.");
        return "redirect:/projects/" + id;
    }
}
