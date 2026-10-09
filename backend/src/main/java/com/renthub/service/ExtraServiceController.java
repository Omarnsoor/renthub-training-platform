package com.renthub.service;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/services") @CrossOrigin(origins="http://localhost:5173")
public class ExtraServiceController {
  private final ExtraServiceRepository repo; public ExtraServiceController(ExtraServiceRepository repo){this.repo=repo;}
  @GetMapping public List<ExtraService> all(){return repo.findAll();}
  @PostMapping public ExtraService create(@RequestBody ExtraService s){return repo.save(s);}
}
