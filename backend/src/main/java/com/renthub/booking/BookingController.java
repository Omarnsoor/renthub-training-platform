package com.renthub.booking;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/bookings") @CrossOrigin(origins="http://localhost:5173")
public class BookingController {
  private final BookingRepository repo; public BookingController(BookingRepository repo){this.repo=repo;}
  @GetMapping public List<Booking> all(){return repo.findAll();}
  @PostMapping public Booking create(@RequestBody Booking b){
    if(!b.getEndDate().isAfter(b.getStartDate())) throw new IllegalArgumentException("endDate must be after startDate");
    return repo.save(b);
  }
  @PatchMapping("/{id}/cancel") public Booking cancel(@PathVariable Long id){
    Booking b=repo.findById(id).orElseThrow(); b.setStatus("CANCELLED"); return repo.save(b);
  }
}
