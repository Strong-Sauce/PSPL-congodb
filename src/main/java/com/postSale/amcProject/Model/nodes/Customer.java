package com.postSale.amcProject.Model.nodes;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Node
public class Customer {
    @Id
    private String custId;
    private String custName;

    @Relationship(type = "PURCHASED", direction = Relationship.Direction.OUTGOING)
    private List<Sale> purchases = new ArrayList<>();
}
