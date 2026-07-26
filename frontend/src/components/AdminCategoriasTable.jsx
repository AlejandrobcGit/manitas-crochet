

import "./AdminCategoriasTable.css";

const API_URL = "http://localhost:8080";

function AdminCategoriasTable({
    categorias,
    onEditar,
    onEliminar
}) {


    return (
        <div className="admin-tabla-container">

            <table className="admin-tabla">

                <thead>
                    <tr>
                        <th>Categoria</th>
                        <th>Acciones</th>
                    </tr>
                </thead>

                <tbody>

                    {categorias.map(categoria => {

                        return (
                            <tr key={categoria.id}>
                                <td>{categoria.nombre}</td>
                                <td>
                                    <div className="admin-tabla__acciones">

                                        <button className="btn-editar"
                                            onClick={() => onEditar(categoria)}
                                        >
                                            Editar
                                        </button>

                                        <button className="btn-eliminar"
                                            onClick={() => onEliminar(categoria.id)}
                                        >
                                            Eliminar
                                        </button>

                                    </div>
                                </td>

                            </tr>
                        );

                    })}
                </tbody>
            </table>
        </div>
    );
}

export default AdminCategoriasTable;