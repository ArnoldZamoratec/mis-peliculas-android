package com.cursosenati.mispeliculas;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PeliculaAdapter extends RecyclerView.Adapter<PeliculaAdapter.PeliculaViewHolder> {

    public interface OnPeliculaClickListener {
        void onPeliculaClick(Pelicula pelicula);
    }

    private ArrayList<Pelicula> listaPeliculas;
    private OnPeliculaClickListener listener;
    private boolean esAdmin = false;

    public PeliculaAdapter(ArrayList<Pelicula> listaPeliculas, OnPeliculaClickListener listener) {
        this(listaPeliculas, false, listener);
    }

    public PeliculaAdapter(ArrayList<Pelicula> listaPeliculas, boolean esAdmin, OnPeliculaClickListener listener) {
        this.listaPeliculas = listaPeliculas;
        this.esAdmin = esAdmin;
        this.listener = listener;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
        notifyDataSetChanged();
    }

    public void actualizarLista(ArrayList<Pelicula> nuevaLista) {
        this.listaPeliculas = nuevaLista;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PeliculaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pelicula, parent, false);
        return new PeliculaViewHolder(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull PeliculaViewHolder holder, int position) {
        Pelicula pelicula = listaPeliculas.get(position);

        holder.txtTitulo.setText(pelicula.getTitulo());
        holder.txtAnio.setText(String.valueOf(pelicula.getAnio()));
        holder.txtGenero.setText(pelicula.getGenero());

        StringBuilder estrellas = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= pelicula.getCalificacion()) {
                estrellas.append("★");
            } else {
                estrellas.append("☆");
            }
        }

        holder.txtCalificacion.setText(estrellas.toString());
        if (holder.txtCalificacionNumero != null) {
            holder.txtCalificacionNumero.setText(pelicula.getCalificacion() + "/5");
        }

        if (pelicula.getImagenUri() != null && !pelicula.getImagenUri().isEmpty()) {
            try {
                holder.imgPelicula.setImageURI(Uri.parse(pelicula.getImagenUri()));
            } catch (Exception e) {
                holder.imgPelicula.setImageResource(pelicula.getImagen());
            }
        } else {
            holder.imgPelicula.setImageResource(pelicula.getImagen());
        }

        View.OnClickListener clickListener = v -> {
            if (listener != null) {
                listener.onPeliculaClick(pelicula);
            }
        };

        holder.itemView.setOnClickListener(clickListener);
        if (holder.btnOpcionesItem != null) {
            holder.btnOpcionesItem.setVisibility(esAdmin ? View.VISIBLE : View.GONE);
            holder.btnOpcionesItem.setOnClickListener(clickListener);
        }
    }

    @Override
    public int getItemCount() {
        return listaPeliculas != null ? listaPeliculas.size() : 0;
    }

    public static class PeliculaViewHolder extends RecyclerView.ViewHolder {

        ImageView imgPelicula;
        TextView txtTitulo;
        TextView txtAnio;
        TextView txtGenero;
        TextView txtCalificacion;
        TextView txtCalificacionNumero;
        ImageButton btnOpcionesItem;

        public PeliculaViewHolder(@NonNull View itemView) {
            super(itemView);

            imgPelicula = itemView.findViewById(R.id.imgPelicula);
            txtTitulo = itemView.findViewById(R.id.txtTitulo);
            txtAnio = itemView.findViewById(R.id.txtAnio);
            txtGenero = itemView.findViewById(R.id.txtGenero);
            txtCalificacion = itemView.findViewById(R.id.txtCalificacion);
            txtCalificacionNumero = itemView.findViewById(R.id.txtCalificacionNumero);
            btnOpcionesItem = itemView.findViewById(R.id.btnOpcionesItem);
        }
    }
}