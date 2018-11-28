package mx.digitalcoaster.bbva_ingenico.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.List;

import mx.digitalcoaster.bbva_ingenico.R;
import mx.digitalcoaster.bbva_ingenico.models.Historial;

/**
 * Created by zertuche on 9/11/15.
 */
public class HistorialArrayAdapter extends ArrayAdapter<Historial> {

    private Context context;
    private int layoutResourceId;
    private List<Historial> listaHistorial;

    public HistorialArrayAdapter(Context context, int position, List<Historial> objects) {
        super(context, position, objects);
        this.context = context;
        this.layoutResourceId = position;
        this.listaHistorial = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        //Comprobando si el View no existe
        if (null == convertView) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            listItemView = inflater.inflate(layoutResourceId, parent, false);
        }

        Historial item = listaHistorial.get(position);
        TextView tvFecha = listItemView.findViewById(R.id.tvFecha);
        TextView tvMonto =  listItemView.findViewById(R.id.tvMonto);
        TextView tvConcepto = listItemView.findViewById(R.id.tvConcepto);
        TextView tvOrden = listItemView.findViewById(R.id.tvOrden);
        TextView tvAprobacion = listItemView.findViewById(R.id.tvAprobacion);
        TextView tvBanco = listItemView.findViewById(R.id.tvBanco);
        TextView tvTipo = listItemView.findViewById(R.id.tvTipo);
        TextView tvMoneda = listItemView.findViewById(R.id.textView24);

        tvFecha.setText(item.getFecha().replace('.', '/') + ", " + item.getHora());
        tvMonto.setText(item.getMonto());
        tvConcepto.setText(item.getConcepto());
        tvOrden.setText(item.getOrden());
        tvAprobacion.setText(item.getAprobacion());
        tvBanco.setText(item.getBanco());

        String tipo;
        //Log.d("MPOS", "Menu: " + tipoMenu + " Orden: " + item.getOrden());

        item.setMostrarCancelar(false);

        switch (item.getTipo()){
            case "1":
                tipo= "Pago";
                if (!item.getSubstatus().equals("C")){
                    item.setMostrarCancelar(true);
                    listItemView.findViewById(R.id.imageView6).setVisibility(View.VISIBLE);
                }
                break;
            case "2":
                tipo= "Cancelación";
                break;
            case "3":
                tipo= "Devolución";
                break;
            case "4":
                tipo= "Contracargo";
                break;
            case "5":
                tipo= "Dispersión";
                break;
            default:
                tipo= "No identificado";
                break;
        }

        tvTipo.setText((tipo));

        ImageView ivTipoBanco = (ImageView) listItemView.findViewById(R.id.ivTipoBanco);
        if (item.getUltimosDigitos()!=""){
            if (item.getUltimosDigitos().charAt(0)=='4'){
                ivTipoBanco.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.visa));
            } else {
                ivTipoBanco.setImageDrawable(ContextCompat.getDrawable(getContext(), R.mipmap.mastercard));
            }
        }

        if(item.getMoneda().equals("USD")){
            tvMoneda.setText("USD");
        }else{
            tvMoneda.setText("MXN");
        }


        return listItemView;
    }

    @Override
    public int getViewTypeCount() {
        // menu type count
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        // current menu type
        //Integer.parseInt(getItem(position).getTipo());
        if (getItem(position).getMostrarCancelar()){
            return 1;
        } else {
            return 2;
        }
    }

    public void update(List<Historial> historials){

    }
}
