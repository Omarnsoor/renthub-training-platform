package com.renthub.maintenance;

import com.renthub.audit.AuditService;
import com.renthub.availability.*;
import com.renthub.car.CarRepository;
import com.renthub.property.PropertyRepository;
import com.renthub.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;

@Service
public class MaintenanceService {
 private final MaintenanceRepository repo; private final AvailabilityBlockRepository blocks; private final CarRepository cars; private final PropertyRepository props; private final AuditService audit;
 public MaintenanceService(MaintenanceRepository repo,AvailabilityBlockRepository blocks,CarRepository cars,PropertyRepository props,AuditService audit){this.repo=repo;this.blocks=blocks;this.cars=cars;this.props=props;this.audit=audit;}

 @Transactional
 public MaintenanceRecord schedule(User user,MaintenanceRecord input){
  String type=input.getAssetType()==null?"":input.getAssetType().toUpperCase();Long owner=owner(user,type,input.getAssetId());if(input.getStartDate()==null)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Maintenance start date is required");LocalDate end=input.getEndDate()==null?input.getStartDate().plusDays(1):input.getEndDate();if(!end.isAfter(input.getStartDate()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Maintenance end date must be after start date");
  input.setAssetType(type);input.setOwnerId(owner);input.setEndDate(end);input.setStatus("SCHEDULED");MaintenanceRecord saved=repo.save(input);
  AvailabilityBlock block=new AvailabilityBlock();block.setAssetType(type);block.setAssetId(input.getAssetId());block.setStartDate(input.getStartDate());block.setEndDate(end);block.setReason("Maintenance #"+saved.getId()+": "+input.getMaintenanceType());block.setBlockType("MAINTENANCE");block.setCreatedBy(user.getId());blocks.save(block);
  audit.record(user.getId(),"MAINTENANCE_SCHEDULED","MAINTENANCE",saved.getId(),null,type+"#"+input.getAssetId());return saved;
 }

 @Transactional
 public MaintenanceRecord status(User user,Long id,String status){MaintenanceRecord m=repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Maintenance record not found"));if(!"ADMIN".equalsIgnoreCase(user.getRole())&&!user.getId().equals(m.getOwnerId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Not your maintenance record");String old=m.getStatus();m.setStatus(status.toUpperCase());repo.save(m);audit.record(user.getId(),"MAINTENANCE_STATUS","MAINTENANCE",id,old,m.getStatus());return m;}

 private Long owner(User u,String type,Long id){if("CAR".equals(type)){var c=cars.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Car not found"));if(!"ADMIN".equalsIgnoreCase(u.getRole())&&!u.getId().equals(c.getOwnerId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this car");return c.getOwnerId();}if("PROPERTY".equals(type)){var p=props.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Property not found"));if(!"ADMIN".equalsIgnoreCase(u.getRole())&&!u.getId().equals(p.getOwnerId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not own this property");return p.getOwnerId();}throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unsupported asset type");}
}
