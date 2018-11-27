package mx.digitalcoaster.bbva_ingenico.models;

/**
 * Created by zertuche on 9/11/15.
 */
public class Historial {
    private String fecha;
    private String hora;
    private String monto;
    private String concepto;
    private String orden;
    private String aprobacion;
    private String banco;
    private String transactionId;
    private String ultimosDigitos;
    private String moneda;
    private String tipo;
    private String substatus;
    private Boolean mostrarCancelar;

    public Historial(String transactionId, String fecha, String hora, String monto, String concepto, String orden, String aprobacion, String banco, String ultimosDigitos, String moneda, String tipo, String substatus) {
        this.fecha = fecha;
        this.hora = hora;
        this.monto = monto;
        this.concepto = concepto;
        this.orden = orden;
        this.aprobacion = aprobacion;
        this.banco = banco;
        this.transactionId = transactionId;
        this.ultimosDigitos = ultimosDigitos;
        this.moneda = moneda;
        this.tipo = tipo;
        this.substatus = substatus;
        this.mostrarCancelar = false;
    }

    public String getBanco() {
        return banco;
    }

    public String getFecha() {
        return fecha;
    }

    public String getHora() {
        return hora;
    }

    public String getMonto() {
        return monto;
    }

    public String getConcepto() {
        return concepto;
    }

    public String getOrden() {
        return orden;
    }

    public String getAprobacion() {
        return aprobacion;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public String getMoneda() {
        return moneda;
    }

    public String getTipo() {
        return tipo;
    }

    public String getSubstatus() {
        return substatus;
    }

    public Boolean getMostrarCancelar(){
        return mostrarCancelar;
    }

    public void setMostrarCancelar(Boolean mostrarCancelar) {
        this.mostrarCancelar = mostrarCancelar;
    }
}