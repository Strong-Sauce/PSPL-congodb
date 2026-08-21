package com.postSale.amcProject.Model.nodes;

import com.postSale.amcProject.Model.enums.ProductCategory;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Node
@Getter
@Setter
public class Product {

    @Id
    private String productSerialNumber;

    private String productName;
    private LocalDate productCreatedDate;
    private ProductCategory productCategory;

    @Relationship(type = "HAS_WARRANTY", direction = Relationship.Direction.OUTGOING)
    private List<Warranty> warrantyList = new ArrayList<>();
}