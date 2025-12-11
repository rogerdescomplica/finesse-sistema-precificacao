package com.finesse.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finesse.entity.Material;
import com.finesse.entity.UnidadeMedida;
import com.finesse.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

class MaterialControllerTest {

    MockMvc mvc;
    MaterialService service;
    @Autowired ObjectMapper om = new ObjectMapper();

    @BeforeEach
    void setup() {
        service = Mockito.mock(MaterialService.class);
        MaterialController controller = new MaterialController(service);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private Material sample() {
        Material m = new Material();
        m.setId(1L);
        m.setProduto("Álcool");
        m.setUnidadeMedida(UnidadeMedida.LITRO);
        m.setVolumeEmbalagem(new BigDecimal("1.00"));
        m.setPrecoEmbalagem(new BigDecimal("10.00"));
        m.setCustoUnitario(new BigDecimal("10.000000"));
        m.setAtivo(true);
        return m;
    }

    @Test @WithMockUser
    void list_ok() throws Exception {
        when(service.list(anyString(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(new PageImpl<>(java.util.List.of(sample()), PageRequest.of(0,10), 1));
        mvc.perform(get("/api/materiais")).andExpect(status().isOk());
    }

    @Test @WithMockUser
    void get_found() throws Exception {
        when(service.findById(eq(1L))).thenReturn(Optional.of(sample()));
        mvc.perform(get("/api/materiais/1")).andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test @WithMockUser
    void get_notfound() throws Exception {
        when(service.findById(eq(2L))).thenReturn(Optional.empty());
        mvc.perform(get("/api/materiais/2")).andExpect(status().isNotFound());
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void create_ok() throws Exception {
        Material m = sample(); m.setId(10L);
        when(service.create(any(Material.class))).thenReturn(m);
        mvc.perform(post("/api/materiais").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(sample())))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", "/api/materiais/10"));
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void update_ok() throws Exception {
        when(service.update(eq(1L), any(Material.class))).thenReturn(Optional.of(sample()));
        mvc.perform(put("/api/materiais/1").contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(sample())))
           .andExpect(status().isOk());
    }

    @Test @WithMockUser(roles = {"ADMIN"})
    void delete_ok() throws Exception {
        when(service.delete(eq(1L))).thenReturn(true);
        mvc.perform(delete("/api/materiais/1")).andExpect(status().isOk());
    }
}
